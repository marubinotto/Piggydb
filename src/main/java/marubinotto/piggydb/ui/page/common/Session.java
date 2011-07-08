package marubinotto.piggydb.ui.page.common;

import java.util.Enumeration;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import marubinotto.piggydb.model.User;
import marubinotto.piggydb.ui.WarSetting;
import marubinotto.util.Assert;
import marubinotto.util.time.DateTime;
import net.sf.click.Context;

public class Session {
	
	private static Log logger = LogFactory.getLog(Session.class);
	
	private static final String SK_CLIENT_ADDRESS = "clientAddress";
	private static final String SK_USER_AGENT = "userAgent";

	private Context context;
	private HttpServletRequest request;
	
	private WarSetting warSetting;
	
	private boolean anonymousEnabled = false;

	public Session(Context context, WarSetting warSetting, boolean anonymousEnabled) {
		this.context = context;
		this.request = this.context.getRequest();
		this.warSetting = warSetting;
		this.anonymousEnabled = anonymousEnabled;
	}
	
	@SuppressWarnings("rawtypes")
	public void log() {
		if (!logger.isDebugEnabled()) return;

		HttpSession session = getHttpSessionIfExists();
		if (session == null) {
			logger.debug("No session.");
			return;
		}

		logger.debug("Session {");
		for (Enumeration e = session.getAttributeNames(); e.hasMoreElements();) {
			logger.debug("  " + e.nextElement());
		}
		logger.debug("}");
	}

	public void start(User user, boolean rememberMe) {
		Assert.Arg.notNull(user, "user");
		
		HttpSession newSession = this.request.getSession(true);
		newSession.setAttribute(User.KEY, user);
		newSession.setAttribute(SK_CLIENT_ADDRESS, this.request.getRemoteAddr());
		newSession.setAttribute(SK_USER_AGENT, this.request.getHeader("User-Agent"));
		
		if (rememberMe) {
			storeSession(newSession);
			user.setSessionPersisted(true);
			logger.debug("Set the session persisted");
		}
	}
	
	public User getUser() {
		if (!this.context.hasSession()) {
			return null;
		}

		// Avoid the session hijacking
		if (!validateRemoteAddress()) {
			this.context.getSession().invalidate();
			logger.warn("Session invalidated for an invalid client address");
			return null;
		}
		if (!validateUserAgent()) {
			this.context.getSession().invalidate();
			logger.warn("Session invalidated for an invalid user agent");
			return null;
		}

		// Get the user object in this session
		User user = (User)this.context.getSessionAttribute(User.KEY);
		if (user == null) return null;

		// While a session is persisted, the setting of anonymous access could be changed
		if (!this.anonymousEnabled && user.isAnonymous()) {
			this.context.getSession().invalidate();
			logger.warn("Invalid anonymous session invalidated");
			return null;
		}

		if (user.hasSessionPersisted()) {
			setSessionCookieWhenPersistentCookieIsAboutToBeExpired(this.context.getSession());
		}
		return user;
	}
	
	public void invalidateIfExists() {
		HttpSession httpSession = getHttpSessionIfExists();
		if (httpSession != null) httpSession.invalidate();
	}
	
	protected HttpSession getHttpSessionIfExists() {
		return this.request.getSession(false);
	}
	
	private boolean validateRemoteAddress() {
		if (!this.warSetting.isClientAddressAuthEnabled()) {
			return true;
		}

		String expected = (String)this.context.getSessionAttribute(SK_CLIENT_ADDRESS);
		String actual = this.request.getRemoteAddr();

		logger.info("Validate the remote address: " + actual + " (expected: " + expected + ")");
		return actual.equals(expected);
	}

	private boolean validateUserAgent() {
		if (!this.warSetting.isUserAgentAuthEnabled()) {
			return true;
		}

		String expected = (String)this.context.getSessionAttribute(SK_USER_AGENT);
		String actual = this.request.getHeader("User-Agent");

		logger.info("Validate the user agent: " + actual + " (expected: " + expected + ")");
		return ObjectUtils.equals(actual, expected);
	}
	
	
	// Session persistence

	private static final int PERSISTED_SESSION_MAX_AGE = 60 * 60 * 24 * 7; // 1 week
	private static final String COOKIE_NAME_SESSION_ID = "JSESSIONID";

	private void storeSession(HttpSession session) {
		session.setMaxInactiveInterval(PERSISTED_SESSION_MAX_AGE);

		Cookie cookie = createSessionCookie(session);
		cookie.setMaxAge(PERSISTED_SESSION_MAX_AGE);
		this.context.getResponse().addCookie(cookie);
	}

	private Cookie createSessionCookie(HttpSession session) {
		Cookie cookie = new Cookie(COOKIE_NAME_SESSION_ID, session.getId());
		cookie.setPath(this.request.getContextPath());
		return cookie;
	}

	private static final long THRESHOLD_TO_BE_EXPIRED = 1000 * 60 * 60; // 1 hour

	private void setSessionCookieWhenPersistentCookieIsAboutToBeExpired(
			HttpSession session) {
		if (isPersistentCookieAboutToBeExpired(session)) {
			this.context.getResponse().addCookie(createSessionCookie(session));
			logger.debug("The persistent Cookie to be overwritten with a session cookie.");
		}
	}

	private static boolean isPersistentCookieAboutToBeExpired(HttpSession session) {
		long sessionAge = DateTime.getCurrentTime().getTime() - session.getCreationTime();
		long left = (PERSISTED_SESSION_MAX_AGE * 1000) - sessionAge;
		return left < THRESHOLD_TO_BE_EXPIRED;
	}
}

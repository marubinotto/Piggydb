package marubinotto.piggydb.api;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import marubinotto.piggydb.model.auth.User;
import marubinotto.util.Assert;
import marubinotto.util.time.DateTime;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class Session {

  private static Log logger = LogFactory.getLog(Session.class);
  
  private HttpServletRequest request;
  private HttpServletResponse response;
  
  private boolean anonymousEnabled = false;
  
  public Session(HttpServletRequest request, HttpServletResponse response, boolean anonymousEnabled) {
    this.request = request;
    this.response = response;
    this.anonymousEnabled = anonymousEnabled;
  }
  
  public void start(User user, Integer maxAgeAsSeconds) {
    Assert.Arg.notNull(user, "user");
    
    HttpSession newSession = this.request.getSession(true);
    newSession.setAttribute(User.KEY, user);
    
    if (maxAgeAsSeconds != null) {
      persistSession(newSession, maxAgeAsSeconds);
      user.setSessionPersisted(true);
      logger.debug("Set the session persisted");
    }
  }
  
  public User getUser() {
    HttpSession session = this.request.getSession(false);
    if (session == null) {
      return null;
    }

    // Get the user object in this session
    User user = (User)session.getAttribute(User.KEY);
    if (user == null) return null;

    // While a session is persisted, the setting of anonymous access could be changed
    if (!this.anonymousEnabled && user.isAnonymous()) {
      session.invalidate();
      logger.warn("Invalid anonymous session invalidated");
      return null;
    }

    if (user.hasSessionPersisted()) {
      setSessionCookieWhenPersistentCookieIsAboutToBeExpired(session);
    }
    return user;
  }
  
  public void invalidateIfExists() {
    HttpSession session = this.request.getSession(false);
    if (session != null) session.invalidate();
  }
  
  
  // Session persistence
  //
  // The current implementation is not safe
  // cf. http://stackoverflow.com/questions/244882/what-is-the-best-way-to-implement-remember-me-for-a-website
  //     http://stackoverflow.com/questions/549/the-definitive-guide-to-form-based-website-authentication
  //     http://fishbowl.pastiche.org/2004/01/19/persistent_login_cookie_best_practice/
  
  private static final String COOKIE_NAME_SESSION_ID = "JSESSIONID";
  private static final long THRESHOLD_TO_BE_EXPIRED = 1000 * 60 * 60; // 1 hour
  
  private void persistSession(HttpSession session, Integer maxAgeAsSeconds) {
    session.setMaxInactiveInterval(maxAgeAsSeconds);

    Cookie cookie = createCookie(session);
    cookie.setMaxAge(maxAgeAsSeconds);
    this.response.addCookie(cookie);
  }
  
  private Cookie createCookie(HttpSession session) {
    Cookie cookie = new Cookie(COOKIE_NAME_SESSION_ID, session.getId());
    cookie.setPath(this.request.getContextPath());
    return cookie;
  }
  
  private Integer getPersistentSessionMaxAge() {
    for (Cookie cookie : this.request.getCookies()) {
      if (cookie.getName().equals(COOKIE_NAME_SESSION_ID) && cookie.getMaxAge() > 0) {
        return cookie.getMaxAge();
      }
    }
    return null;
  }
  
  private void setSessionCookieWhenPersistentCookieIsAboutToBeExpired(HttpSession session) {
    if (isPersistentCookieAboutToBeExpired(session)) {
      this.response.addCookie(createCookie(session));
      logger.debug("The persistent Cookie to be overwritten with a session cookie.");
    }
  }

  private boolean isPersistentCookieAboutToBeExpired(HttpSession session) {
    Integer maxAge = getPersistentSessionMaxAge();
    if (maxAge == null) return false;
    
    long sessionAge = DateTime.getCurrentTime().getTime() - session.getCreationTime();
    long left = (maxAge * 1000) - sessionAge;
    return left < THRESHOLD_TO_BE_EXPIRED;
  }
}

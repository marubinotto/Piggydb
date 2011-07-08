package marubinotto.piggydb.ui.page.common;

import java.io.UnsupportedEncodingException;
import java.util.Enumeration;

import javax.servlet.http.HttpServletRequest;

import marubinotto.piggydb.model.User;
import marubinotto.piggydb.ui.WarSetting;
import marubinotto.piggydb.ui.page.HomePage;
import marubinotto.piggydb.ui.page.LoginPage;
import marubinotto.piggydb.ui.page.model.SelectedFragments;
import marubinotto.util.Assert;
import marubinotto.util.time.StopWatch;
import marubinotto.util.web.WebMessageSource;
import marubinotto.util.web.WebUtils;
import net.sf.click.Page;

import org.apache.commons.codec.net.URLCodec;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.UnhandledException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

public abstract class AbstractWebResource extends Page 
implements ApplicationContextAware, WebMessageSource {

	public static final String CHAR_ENCODING = "UTF-8";

	public static final String SK_MESSAGE = "message";
	private static final String SK_STOP_WATCH = "stopWatch";

	public PageUrl thisPageUrl;
	public WebResourcePaths resources;
	public WebMessageSource messageSource = this;
	public User user;
	
	private Log logger;
	private ApplicationContext applicationContext;
	private DomainModelBeans domain;
	private Session session;
	
	public AbstractWebResource() {
	}

	protected Log getLogger() {
		if (this.logger == null) {
			this.logger = LogFactory.getLog(getClass());
		}
		return this.logger;
	}

	// WebMessageSource

	@Override
	public String getMessage(String name, Object arg) {
		return getMessage(name, arg, true);
	}

	@Override
	public String getMessage(String name, Object[] args) {
		return getMessage(name, args, true);
	}

	public String getMessage(String name, Object arg, boolean escapeArg) {
		return getMessage(name, new Object[] { arg }, escapeArg);
	}

	public String getMessage(String name, Object[] args, boolean escapeArgs) {
		if (escapeArgs) {
			Object[] escaped = new Object[args.length];
			for (int i = 0; i < args.length; i++)
				escaped[i] = WebUtils.escapeHtml(args[i]);
			return super.getMessage(name, escaped);
		}
		else {
			return super.getMessage(name, args);
		}
	}

	// ApplicationContextAware

	public void setApplicationContext(ApplicationContext applicationContext) {
		this.applicationContext = applicationContext;
		this.domain = new DomainModelBeans(this.applicationContext);
	}

	public ApplicationContext getApplicationContext() {
		return this.applicationContext;
	}

	protected Object getBean(String beanName) {
		return this.applicationContext.getBean(beanName);
	}

	public DomainModelBeans getDomain() {
		return this.domain;
	}

	protected final WarSetting getWarSetting() {
		return (WarSetting)getBean("warSetting");
	}

	// Access control

	protected boolean needsAuthentication() {
		return true;
	}

	public Session getSession() {
		return this.session;
	}

	public User getUser() {
		return this.user;
	}

	public boolean isAuthenticated() {
		return this.user != null;
	}

	protected String[] getAuthorizedRoles() {
		return null;
	}

	private boolean isAuthorized() {
		getLogger().info("Authorizing ...");
		String[] authorizedRoles = getAuthorizedRoles();
		if (authorizedRoles == null) {
			return true;
		}
		for (int i = 0; i < authorizedRoles.length; i++) {
			if (this.user.isInRole(authorizedRoles[i])) {
				return true;
			}
		}
		return false;
	}

	@Override
	public final boolean onSecurityCheck() {
		logParameters();
	
		this.session = new Session(
			getContext(), 
			getWarSetting(), 
			getDomain().getAuthentication().isEnableAnonymous());
		this.session.log();
		
		createStopWatchIfNeeded();

		// Get a user object if it exists
		this.user = this.session.getUser();
		if (this.user == null) this.user = autoLoginAsAnonymous();
		if (this.user != null) {
			// for marubinotto.util.web.CustomizedSecurityRequestWrapper (for click menu auth)
			getContext().getRequest().setAttribute(User.KEY, this.user);
		}

		// Check security if needed
		if (needsAuthentication()) {
			if (this.user == null) {
				setRedirectToLogin();
				return false;
			}
			if (!isAuthorized()) {
				setRedirectWithMessage(HomePage.class, getMessage("no-authority-for-page"));
				return false;
			}
		}

		// Preparation before init()
		this.resources = new WebResourcePaths(
			getContext().getRequest().getContextPath(), 
			getWarSetting().getPiggydbVersion());
		try {
			return onPreInit();
		}
		catch (RuntimeException e) {
			throw e;
		}
		catch (Exception e) {
			throw new UnhandledException(e);
		}
	}

	protected void setRedirectToLogin() {
		String path = getContext().getPagePath(LoginPage.class);
		setRedirect(path + "?original=" + encodedOriginalPath());
	}

	private String encodedOriginalPath() {
		HttpServletRequest request = getContext().getRequest();
		String path = StringUtils.defaultString(request.getServletPath())
				+ StringUtils.defaultString(request.getPathInfo());
		String queryString = request.getQueryString();
		if (StringUtils.isNotBlank(queryString)) {
			path = path + "?" + queryString;
		}
		try {
			return new URLCodec().encode(path, CHAR_ENCODING);
		}
		catch (UnsupportedEncodingException e) {
			throw new UnhandledException(e);
		}
	}

	@SuppressWarnings("rawtypes")
	private void logParameters() {
		if (!getLogger().isDebugEnabled()) return;

		getLogger().debug("Parameters {");
		for (Enumeration e = getContext().getRequest().getParameterNames(); e.hasMoreElements();) {
			String name = (String) e.nextElement();
			getLogger().debug("  " + name + " = " + 
				ArrayUtils.toString(getContext().getRequest().getParameterValues(name)));
		}
		getLogger().debug("}");
	}

	protected User autoLoginAsAnonymous() {
		User user = getDomain().getAuthentication().authenticateAsAnonymous();
		if (user == null) return null;

		getSession().start(user, false);
		getLogger().debug("Anonymous session created");
		return user;
	}

	// Workflow

	/**
	 * A target entity object should be prepared here (if this page is for an
	 * entity).
	 */
	protected boolean onPreInit() throws Exception {
		return true;
	}

	@Override
	public void onInit() {
		super.onInit();
		this.thisPageUrl = createThisPageUrl(); // A target entity object maybe
																						// needed
	}

	protected PageUrl createThisPageUrl() {
		return new PageUrl(getClass(), getContext());
	}

	protected String getFullPageUrl() {
		return WebUtils.makeContextUrl(getContext().getRequest()) + getPath();
	}

	protected String getDefaultAtomUrl() {
		return StringUtils.replace(getFullPageUrl(), ".htm", ".atom");
	}

	@Override
	public void onRender() {
		super.onRender();

		if (!needsStopWatch()) discardStopWatch();

		try {
			setModels();
		}
		catch (Exception e) {
			throw new UnhandledException(e);
		}
	}

	protected void setModels() throws Exception {
	}

	// Utilities

	protected void disableClientCaching() {
		getContext().getResponse().addHeader("Cache-Control",
				"no-store, max-age=0, no-cache, must-revalidate");
	}

	protected String modifyIfGarbledByTomcat(String value) {
		try {
			return WebUtils.modifyIfGarbledByTomcat(value, getContext().getCharset(),
					getContext().getRequest(), getContext().getServletContext());
		}
		catch (UnsupportedEncodingException e) {
			throw new UnhandledException(e);
		}
	}

	public void setRedirectToThisPage() {
		Assert.Property.requireNotNull(thisPageUrl, "thisPageUrl");
		setRedirect(this.thisPageUrl.getPagePath());
	}

	public void setRedirectToThisPage(String message) {
		Assert.Property.requireNotNull(thisPageUrl, "thisPageUrl");
		setRedirectWithMessage(this.thisPageUrl.getPagePath(), message);
	}

	public void setRedirectWithMessage(Class<? extends Page> pageClass,
			String message) {
		Assert.Arg.notNull(pageClass, "pageClass");
		Assert.Arg.notNull(message, "message");

		setFlashMessage(message);
		setRedirect(pageClass);
	}

	public void setRedirectWithMessage(String path, String message) {
		Assert.Arg.notNull(path, "path");
		Assert.Arg.notNull(message, "message");

		setFlashMessage(message);
		setRedirect(path);
	}

	public void setFlashMessage(String message) {
		getContext().setFlashAttribute(SK_MESSAGE, message);
	}

	protected String getSessionMessage() {
		return (String) getContext().getSessionAttribute(SK_MESSAGE);
	}

	protected boolean needsStopWatch() {
		return false;
	}

	private void createStopWatchIfNeeded() {
		if (!needsStopWatch()) return;
		if (!getContext().hasSession()) return;

		StopWatch stopWatch = (StopWatch) getContext().getSessionAttribute(SK_STOP_WATCH);
		if (stopWatch == null) stopWatch = new StopWatch(getPath());
		getContext().setFlashAttribute(SK_STOP_WATCH, stopWatch);
	}

	private void discardStopWatch() {
		if (!getContext().hasSession()) return;

		getContext().removeSessionAttribute(SK_STOP_WATCH);
	}

	@SuppressWarnings("unchecked")
	protected <T> T createOrGetObjectInSession(String name, Factory<T> factory) {
		Assert.Arg.notNull(name, "name");
		Assert.Arg.notNull(factory, "factory");

		T object = (T) getContext().getSessionAttribute(name);
		if (object == null) {
			object = factory.create();
			getContext().setSessionAttribute(name, object);
		}
		return object;
	}

	protected static interface Factory<T> {
		public T create();
	}
	
	public static final String SK_SELECTED_FRAGMENTS = "selectedFragments";
	
	protected SelectedFragments getSelectedFragments() {
		return createOrGetObjectInSession(
			SK_SELECTED_FRAGMENTS, 
			new Factory<SelectedFragments>() {
				public SelectedFragments create() {
					return new SelectedFragments();
				}
			});
	}
}

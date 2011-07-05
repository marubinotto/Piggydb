package marubinotto.piggydb.ui.page;

import java.util.Map;

import marubinotto.piggydb.model.Filter;
import marubinotto.piggydb.model.Fragment;
import marubinotto.piggydb.model.FragmentsOptions;
import marubinotto.piggydb.model.Tag;
import marubinotto.piggydb.model.entity.RawFilter;
import marubinotto.piggydb.ui.page.command.Logout;
import marubinotto.piggydb.ui.page.model.RecentlyViewed;
import marubinotto.piggydb.ui.page.model.RecentlyViewed.Entity;
import marubinotto.util.paging.Page;
import marubinotto.util.paging.PageUtils;
import net.sf.click.control.PageLink;
import net.sf.click.extras.control.Menu;

import org.apache.commons.lang.ClassUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.UnhandledException;
import org.apache.commons.lang.text.StrBuilder;

public abstract class AbstractBorderPage extends ModelFactory {

	public AbstractBorderPage() {
	}

	@Override
	public String getTemplate() {
		return "/border-template.htm";
	}

	@Override
	protected boolean needsStopWatch() {
		return true;
	}

	//
	// Control
	//

	public Menu rootMenu;
	public PageLink logoutLink = new PageLink(Logout.class);

	@Override
	public void onInit() {
		super.onInit();
		this.rootMenu = Menu.getRootMenu(); // "rootMenu" is the default name
	}

	//
	// Model
	//

	// an ID of an element that should be the top of the page
	public static final String SK_SCROLL_TOP_ELEMENT = "scrollTopElement";
	public static final String SK_RECENTLY_VIEWED = "recentlyViewed";

	public static final String MK_MESSAGE = "message";

	public String title;
	public String htmlTitle;
	public static final String HTML_TITLE_SEP = " - ";

	private static String APP_CSS_IMPORTS;
	public String appCssImports;

	private static String APP_JS_IMPORTS;
	public String appJsImports;

	public StrBuilder appAdditionalCssImports = new StrBuilder();
	public StrBuilder appAdditionalJsImports = new StrBuilder();

	public Page<Fragment> bookmarkedFragments = EMPTY_FRAGMENTS;
	public Tag bookmarkTag;

	public Page<Filter> filters;

	public Map<Entity, String> recentlyViewed;

	@Override
	protected void setModels() throws Exception {
		super.setModels();

		setAppCssImports();
		setAppJsImports();

		this.title = getMessage("application-title");
		this.htmlTitle = getPageTitle(ClassUtils.getShortClassName(getClass()),
			this);
		showSessionMessageIfExist();
	}

	public static String getPageTitle(String pageName, AbstractPage page)
		throws Exception {
		String pageTitle = page.getMessage(pageName + "-htmlTitle");
		pageTitle = StringUtils.isNotBlank(pageTitle) ? (" - " + pageTitle) : "";
		return page.getGlobalSetting().getDatabaseTitle() + pageTitle;
	}

	protected void setMessage(String message) {
		addModel(MK_MESSAGE, message);
	}

	private void showSessionMessageIfExist() {
		String sessionMessage = getSessionMessage();
		if (sessionMessage != null) {
			setMessage(sessionMessage);
		}
	}

	private void setAppCssImports() {
		if (APP_CSS_IMPORTS == null) {
			getLogger().debug("Initializing APP_CSS_IMPORTS ...");
			StrBuilder imports = new StrBuilder();
			imports.appendln(this.html.cssImport("style/prettify.css", true, null));
			imports.appendln(this.html.cssImport("style/watermark.css", true, null));
			imports.appendln(this.html.cssImport("style/curve/curve.css", true, "screen"));
			imports.appendln(this.html.cssImport("style/tree/tree.css", true, null));
			imports.appendln(this.html.cssImport("style/facebox/facebox.css", true, null));
			imports.appendln(this.html.cssImport("style/piggydb-base.css", true, "screen"));
			imports.appendln(this.html.cssImport("style/piggydb-shared.css", true, "screen"));
			imports.appendln(this.html.cssImport("style/piggydb-wiki-help.css", true, null));
			imports.appendln(this.html.cssImport("style/piggydb-print.css", true, "print"));
			imports.appendln(this.html.cssImport("jquery-ui-1.8.6/themes/base/jquery.ui.all.css", false, "screen"));
			imports.appendln(this.html.cssImport("autocomplete/jquery.autocomplete-1.1-1.css", false, "screen"));
			APP_CSS_IMPORTS = imports.toString();
		}
		this.appCssImports = APP_CSS_IMPORTS;
	}

	private void setAppJsImports() {
		if (APP_JS_IMPORTS == null) {
			getLogger().debug("Initializing APP_JS_IMPORTS ...");
			StrBuilder imports = new StrBuilder();
			imports.appendln(this.html.jsImport("scripts/jquery-1.4.2.min.js", false));
			imports.appendln(this.html.jsImport("jquery-ui-1.8.6/jquery-ui-1.8.6.custom.min.js", false));
			imports.appendln(this.html.jsImport("scripts/purePacked.js", false));
			imports.appendln(this.html.jsImport("scripts/prettify-8.js", false));
			imports.appendln(this.html.jsImport("scripts/jquery.updnWatermark.js", true));
			imports.appendln(this.html.jsImport("autocomplete/jquery.bgiframe.min.js", false));
			imports.appendln(this.html.jsImport("autocomplete/jquery.ajaxQueue.js", false));
			imports.appendln(this.html.jsImport("autocomplete/jquery.autocomplete-1.1-modified.js", true));
			imports.appendln(this.html.jsImport("scripts/piggydb.js", true));
			imports.appendln(this.html.jsImport("scripts/piggydb-jquery.js", true));
			imports.appendln(this.html.jsImport("scripts/piggydb-widgets.js", true));
			APP_JS_IMPORTS = imports.toString();
		}
		this.appJsImports = APP_JS_IMPORTS;
	}

	protected void importCssFile(String filePath, boolean versioning, String media) {
		this.appAdditionalCssImports.appendln(this.html.cssImport(filePath,
			versioning, media));
	}

	protected void importJsFile(String filePath, boolean versioning) {
		this.appAdditionalJsImports.appendln(this.html.jsImport(filePath,
			versioning));
	}

	protected static final Page<Fragment> EMPTY_FRAGMENTS = PageUtils.empty(0);

	// Sidebar contents

	protected void setCommonSidebarModels() throws Exception {
		setBookmarkedFragments();
		setFilters();
		setRecentlyViewed();
	}

	public static final int BOOKMARK_SIZE = 100;

	protected void setBookmarkedFragments() throws Exception {
		this.bookmarkTag = getTagRepository().getByName(Tag.NAME_BOOKMARK);
		if (this.bookmarkTag == null) return;

		RawFilter filter = new RawFilter();
		filter.getClassification().addTag(this.bookmarkTag);

		Tag trashTag = getTagRepository().getTrashTag();
		if (trashTag != null) filter.getExcludes().addTag(trashTag);

		this.bookmarkedFragments = getFragmentRepository().findByFilter(filter,
			new FragmentsOptions(BOOKMARK_SIZE, 0, false));
	}

	protected void setFilters() throws Exception {
		this.filters = getFilterRepository().getRecentChanges(
			ALMOST_UNLIMITED_PAGE_SIZE, 0);
	}

	protected RecentlyViewed getRecentlyViewed() {
		return createOrGetObjectInSession(SK_RECENTLY_VIEWED,
			new Factory<RecentlyViewed>() {
				public RecentlyViewed create() {
					return new RecentlyViewed(20);
				}
			});
	}

	protected void setRecentlyViewed() {
		try {
			this.recentlyViewed = getRecentlyViewed().getAllWithNames(
				getFragmentRepository(), getTagRepository(), getFilterRepository());
		}
		catch (Exception e) {
			throw new UnhandledException(e);
		}
	}
}

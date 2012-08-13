package marubinotto.piggydb.ui.page.common;

import java.util.Map;

import marubinotto.piggydb.model.Filter;
import marubinotto.piggydb.model.Fragment;
import marubinotto.piggydb.model.FragmentsOptions;
import marubinotto.piggydb.model.Tag;
import marubinotto.piggydb.model.entity.RawFilter;
import marubinotto.piggydb.ui.page.command.Logout;
import marubinotto.piggydb.ui.page.control.UserMenu;
import marubinotto.piggydb.ui.page.model.RecentlyViewed;
import marubinotto.piggydb.ui.page.model.RecentlyViewed.Entity;
import marubinotto.util.paging.Page;
import marubinotto.util.paging.PageUtils;
import net.sf.click.control.PageLink;
import net.sf.click.extras.control.Menu;

import org.apache.commons.lang.ClassUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.UnhandledException;

public abstract class AbstractBorderPage extends AbstractMainUiHtml {

	public AbstractBorderPage() {
	}

	@Override
	public String getTemplate() {
		return "/border-template.htm";
	}

	//
	// Control
	//

	public Menu rootMenu;
	public UserMenu userMenu = UserMenu.getInstance();
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
	
	public PageImports appPageImports;

	public Page<Fragment> bookmarkedFragments = EMPTY_FRAGMENTS;
	public Tag bookmarkTag;

	public Page<Filter> filters;

	public Map<Entity, String> recentlyViewed;

	@Override
	protected void setModels() throws Exception {
		super.setModels();

		this.appPageImports = new PageImports(this.html);

		this.title = getMessage("application-title");
		this.htmlTitle = getPageTitle(ClassUtils.getShortClassName(getClass()), this);
		showSessionMessageIfExist();
	}

	public static String getPageTitle(String pageName, AbstractWebResource page)
		throws Exception {
		String pageTitle = page.getMessage(pageName + "-htmlTitle");
		pageTitle = StringUtils.isNotBlank(pageTitle) ? (" - " + pageTitle) : "";
		return page.getDomain().getGlobalSetting().getDatabaseTitle() + pageTitle;
	}

	protected void setMessage(String message) {
		addModel(MK_MESSAGE, message);
	}

	private void showSessionMessageIfExist() {
		String sessionMessage = getSession().getMessage();
		if (sessionMessage != null) {
			setMessage(sessionMessage);
		}
	}

	protected void importCss(String filePath, boolean versioning, String media) {
		this.appPageImports.importCss(filePath, versioning, media);
	}

	protected void importBottomJs(String filePath, boolean versioning) {
		this.appPageImports.importBottomJs(filePath, versioning);
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
		this.bookmarkTag = getDomain().getTagRepository().getByName(Tag.NAME_BOOKMARK);
		if (this.bookmarkTag == null) return;

		RawFilter filter = new RawFilter();
		filter.getClassification().addTag(this.bookmarkTag);

		Tag trashTag = getDomain().getTagRepository().getTrashTag();
		if (trashTag != null) filter.getExcludes().addTag(trashTag);

		this.bookmarkedFragments = getDomain().getFragmentRepository().findByFilter(
			filter, new FragmentsOptions(BOOKMARK_SIZE, 0, false));
	}

	protected void setFilters() throws Exception {
		this.filters = getDomain().getFilterRepository().getRecentChanges(
			ALMOST_UNLIMITED_PAGE_SIZE, 0);
	}

	protected RecentlyViewed getRecentlyViewed() {
		return getSession().createOrGet(SK_RECENTLY_VIEWED,
			new Session.Factory<RecentlyViewed>() {
				public RecentlyViewed create() {
					return new RecentlyViewed(20);
				}
			});
	}

	protected void setRecentlyViewed() {
		try {
			this.recentlyViewed = getRecentlyViewed().getAllWithNames(
				getDomain().getFragmentRepository(), 
				getDomain().getTagRepository(), 
				getDomain().getFilterRepository());
		}
		catch (Exception e) {
			throw new UnhandledException(e);
		}
	}
}

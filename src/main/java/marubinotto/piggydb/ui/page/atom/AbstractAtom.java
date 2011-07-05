package marubinotto.piggydb.ui.page.atom;

import java.util.List;

import org.apache.commons.lang.ClassUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.UnhandledException;

import marubinotto.piggydb.model.Fragment;
import marubinotto.piggydb.model.FragmentsOptions;
import marubinotto.piggydb.model.GlobalSetting;
import marubinotto.piggydb.ui.page.AbstractBorderPage;
import marubinotto.piggydb.ui.page.ModelFactory;
import marubinotto.util.web.WebUtils;

public abstract class AbstractAtom extends ModelFactory {
	
	public AbstractAtom() {
	}
	
	@Override
	public String getContentType() {
		return "application/atom+xml";
	}

	@Override
    public String getTemplate() {
        return "/border-template.atom";
    }	

	@Override
	public void onInit() {
		super.onInit();
		setForward((String)null);	
	}
	
	
	//
	// Model
	//

	public String databaseId;
	
	public String feedId;
	public String feedTitle;
	public String feedUrl;
	public String htmlUrl;
	public String feedUpdated;
	
	public String contextUrl;
	
	public List<Fragment> fragments;
	
	protected FragmentsOptions fragmentsOptions = new FragmentsOptions();

	@Override
	public void onRender() {
		super.onRender();
		
		this.fragmentsOptions.setPagingOption(getWarSetting().getFeedMaxSize(), 0);
		this.fragmentsOptions.eagerFetching = true;
		try {
			setFeedInfo();
			this.fragments = getFragments();
		} 
		catch (RuntimeException e) {
			throw e;
		}
		catch (Exception e) {
			throw new UnhandledException(e);
		}
		
		if (this.fragments != null && this.fragments.size() > 0) {
			this.feedUpdated = this.fragments.get(0).getUpdateDatetime().formatAsISO8601();
		}
	}
	
	public static final String PARAM_PREFIX_IN_ID = "-";

	protected void setFeedInfo() throws Exception {
		this.databaseId = getGlobalSetting().getDatabaseId();
		this.feedId = this.databaseId + GlobalSetting.URI_SEP + getName();
		this.feedTitle = AbstractBorderPage.getPageTitle(getTargetPageName(), this);
		
		this.contextUrl = WebUtils.makeContextUrl(getContext().getRequest());
		this.resources.setContextPath(this.contextUrl);
		
		this.feedUrl = getFullPageUrl();
		this.htmlUrl = StringUtils.replace(this.feedUrl, ".atom", ".htm");
		
		this.feedUpdated = getGlobalSetting().getDatabaseTimestamp().formatAsISO8601();
	}
	
	protected void appendQueryToUrls(String query) {
		this.feedUrl = this.feedUrl + query;
		this.htmlUrl = this.htmlUrl +query;
	}
	
	protected String getName() {
		String name = getPath().substring(1);
		return StringUtils.replace(name, ".atom", "");
	}
	
	private String getTargetPageName() {
		String className = ClassUtils.getShortClassName(getClass());
		return StringUtils.replace(className, "Atom", "Page");
	}
	
	protected abstract List<Fragment> getFragments() throws Exception;
}

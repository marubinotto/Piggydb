package marubinotto.piggydb.ui.page;

import marubinotto.piggydb.model.Tag;
import marubinotto.piggydb.ui.page.util.PageUrl;
import marubinotto.piggydb.util.PiggydbUtils;
import marubinotto.util.paging.Page;

import org.apache.commons.lang.StringUtils;

public class SearchPage extends AbstractFragmentsPage {
	
	@Override
    protected PageUrl createThisPageUrl() {
    	PageUrl pageUrl = super.createThisPageUrl();
    	if (this.keywords != null) {
    		pageUrl.parameters.put(PN_KEYWORDS, this.keywords);
    	}
    	return pageUrl;
	}

	
	//
	// Input
	//
	
	public static final String PN_KEYWORDS = "keywords";	

	public String keywords;
	public boolean jtt = false;	// jump to tag
	
	@Override
	protected boolean onPreInit() throws Exception {
		// Garbled parameters should be modified before createThisPageUrl
		this.keywords = modifyIfGarbledByTomcat(this.keywords);
		
		// Jump to tag
		if (this.jtt && this.keywords != null) {
			Tag tag = getTagRepository().getByName(this.keywords);
			if (tag != null) {
				setRedirect(getContext().getPagePath(TagPage.class) + "?id=" + tag.getId());
				return false;
			}
		}
		
		return true;
	}
	
	
	//
	// Model
	//
	
	public String[] keywordList;
	public Page<Tag> tags;

	@Override 
	protected void setModels() throws Exception {
		super.setModels();
		
		importCssFile("style/piggydb-search.css", true, null);
		importJsFile("scripts/jquery.highlightRegex-2.js", false);
		
		if (StringUtils.isNotBlank(this.keywords))
			this.keywordList = PiggydbUtils.splitToKeywords(this.keywords);
		
		this.tags = getTagRepository().findByKeywords(
			this.keywords, ALMOST_UNLIMITED_PAGE_SIZE, 0);
		
		setCommonSidebarModels();
	}

	@Override
	public void onRender() {
		super.onRender();
		embedCurrentStateInParameters();
	}
	
	private void embedCurrentStateInParameters() {
    	if (this.keywords != null) {
    		addParameterToCommonForms(PN_KEYWORDS, this.keywords);
    	}
	}
}

package marubinotto.piggydb.ui.page;

import marubinotto.piggydb.model.Tag;
import marubinotto.piggydb.ui.page.common.AbstractFragmentsPage;
import marubinotto.piggydb.ui.page.common.PageUrl;
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
	public boolean jump = false;

	@Override
	protected boolean onPreInit() throws Exception {
		// Garbled parameters should be modified before createThisPageUrl
		this.keywords = modifyIfGarbledByTomcat(this.keywords);

		// Jump
		if (this.jump && this.keywords != null) {
			this.keywords = this.keywords.trim();
			
			// 1) keywords as a fragment ID
			if (StringUtils.isNumeric(this.keywords)) {
				long id = Long.parseLong(this.keywords);
				if (getDomain().getFragmentRepository().containsId(id)) {
					setRedirect(getContext().getPagePath(FragmentPage.class) + "?id=" + id);
					return false;
				}
			}
			
			// 2) keywords as a tag name
			Tag tag = getDomain().getTagRepository().getByName(this.keywords);
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

		importCss("style/piggydb-search.css", true, null);
		importBottomJs("js/vendor/highlightRegex.js", true);

		if (StringUtils.isNotBlank(this.keywords)) this.keywordList = PiggydbUtils.splitToKeywords(this.keywords);

		this.tags = getDomain().getTagRepository().
			findByKeywords(this.keywords, ALMOST_UNLIMITED_PAGE_SIZE, 0);

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

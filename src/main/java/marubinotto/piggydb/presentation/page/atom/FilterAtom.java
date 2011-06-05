package marubinotto.piggydb.presentation.page.atom;

import java.util.List;

import marubinotto.piggydb.model.Filter;
import marubinotto.piggydb.model.Fragment;
import marubinotto.piggydb.presentation.page.BorderPage;

public class FilterAtom extends AbstractAtom {

	public Long id;
	
	private Filter filter;
	
	@Override
	protected void setFeedInfo() throws Exception {
		super.setFeedInfo();
		
		if (this.id == null) return;
		
		this.feedId = this.feedId + PARAM_PREFIX_IN_ID + this.id;
		appendQueryToUrls("?id=" + this.id);
		
		this.filter = getFilterRepository().get(this.id);
		if (this.filter == null) return;
		
		this.feedTitle  = this.feedTitle + BorderPage.HTML_TITLE_SEP + this.filter.getName();	
	}
	
	@Override
	protected List<Fragment> getFragments() throws Exception {
		if (this.filter == null) return null;
		return getFragmentRepository().findByFilter(this.filter, this.fragmentsOptions);
	}
}

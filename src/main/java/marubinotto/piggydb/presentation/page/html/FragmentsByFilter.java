package marubinotto.piggydb.presentation.page.html;

import marubinotto.piggydb.model.Filter;
import marubinotto.piggydb.presentation.page.FilterPage;

public class FragmentsByFilter extends AbstractFragments {

	public Long id;
	
	public Filter filter;
	
	@Override 
	protected void setFragments() throws Exception {
		if (this.id != null)
			this.filter = getFilterRepository().get(this.id);
		else
			this.filter = (Filter)getContext().getSessionAttribute(FilterPage.SK_NEW_FILTER);
		if (this.filter == null) return;
		
		this.contextTags = this.filter.getClassification();
		this.fragments = getFragmentRepository().findByFilter(this.filter, this.options);
	}
}

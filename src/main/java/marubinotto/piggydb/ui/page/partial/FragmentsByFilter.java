package marubinotto.piggydb.ui.page.partial;

import static org.apache.commons.lang.StringUtils.isNotBlank;
import marubinotto.piggydb.model.Filter;
import marubinotto.piggydb.ui.page.FilterPage;

public class FragmentsByFilter extends AbstractFragments {

	public Long id;
	
	public Filter filter;
	
	@Override 
	protected void setFragments() throws Exception {
		this.label = this.html.filterIconMini();
		
		if (this.id != null) {
			this.filter = getDomain().getFilterRepository().get(this.id);
			if (this.filter != null)
				this.label += " " + this.filter.getName();
		}
		else {
			this.filter = (Filter)getContext().getSessionAttribute(FilterPage.SK_NEW_FILTER);
			this.label += " " + getMessage("FilterPage-new-filter");
		}
		
		if (this.filter == null) return;
		
		this.contextTags = this.filter.getClassification();
		
		marubinotto.piggydb.model.query.FragmentsByFilter query = 
			(marubinotto.piggydb.model.query.FragmentsByFilter)getQuery(
				marubinotto.piggydb.model.query.FragmentsByFilter.class);
		query.setFilter(this.filter);
		if (isNotBlank(this.query)) {
      query.setKeywords(this.query);
      setKeywordRegex(this.query);
    }
		this.fragments = getPage(query);
		
		appendKeywordSearchLabel();
	}
}

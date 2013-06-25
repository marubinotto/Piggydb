package marubinotto.piggydb.ui.page.partial;

import marubinotto.piggydb.model.Filter;
import marubinotto.piggydb.ui.page.FilterPage;

public class FragmentsByFilter extends AbstractFragments {

	public Long id;
	
	@Override 
  protected Filter createFilter() throws Exception {
	  this.label = this.html.filterIconMini();
    
	  Filter filter = null;
    if (this.id != null) {
      filter = getDomain().getFilterRepository().get(this.id);
      if (this.filter != null)
        this.label += " " + this.filter.getName();
    }
    else {
      filter = (Filter)getContext().getSessionAttribute(FilterPage.SK_NEW_FILTER);
      this.label += " " + getMessage("FilterPage-new-filter");
    }
    return filter;
	}
}

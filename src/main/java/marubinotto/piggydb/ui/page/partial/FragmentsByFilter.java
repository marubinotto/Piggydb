package marubinotto.piggydb.ui.page.partial;

import static marubinotto.util.web.WebUtils.escapeHtml;
import marubinotto.piggydb.model.Filter;
import marubinotto.piggydb.ui.page.FilterPage;

public class FragmentsByFilter extends AbstractFragments {

	public Long id;
	
	@Override 
  protected Filter createFilter() throws Exception {
	  Filter filter = null;
	  this.label = this.html.filterIconMini();
    if (this.id != null) {
      filter = getDomain().getFilterRepository().get(this.id);
      if (filter != null) this.label += " " + escapeHtml(filter.getName());
    }
    else {
      filter = (Filter)getContext().getSessionAttribute(FilterPage.SK_NEW_FILTER);
      this.label += " " + getMessage("FilterPage-new-filter");
    }
    this.label += ": ";
    return filter;
	}
}

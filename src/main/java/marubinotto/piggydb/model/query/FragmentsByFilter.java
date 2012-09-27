package marubinotto.piggydb.model.query;

import marubinotto.piggydb.model.Filter;

public interface FragmentsByFilter extends FragmentsQuery {

	public void setFilter(Filter filter);
}

package marubinotto.piggydb.impl.query;

import java.util.ArrayList;
import java.util.List;

import marubinotto.piggydb.model.Filter;
import marubinotto.piggydb.model.Fragment;
import marubinotto.piggydb.model.query.FragmentsByFilter;
import marubinotto.piggydb.model.query.FragmentsQuery;
import marubinotto.util.paging.Page;
import marubinotto.util.paging.PageUtils;

public abstract class H2FragmentsByPredefinedFilterBase extends H2FragmentsQueryBase {

	protected abstract Filter createFilter() throws Exception;

	private FragmentsQuery getQuery() throws Exception {
		Filter filter = createFilter();
		if (filter == null) return null;
		
		FragmentsByFilter query = (FragmentsByFilter)getDelegateeQuery(FragmentsByFilter.class);
		query.setFilter(filter);		
		return query;
	}
	
	public List<Fragment> getAll() throws Exception {
		FragmentsQuery query = getQuery();
		return query != null ? query.getAll() : new ArrayList<Fragment>();
	}
	
	public Page<Fragment> getPage(int pageSize, int pageIndex) throws Exception {
		FragmentsQuery query = getQuery();
		return query != null ? query.getPage(pageSize, pageIndex) : PageUtils.<Fragment>empty(pageSize);
	}
	
	protected void appendFromWhere(StringBuilder sql, List<Object> args) throws Exception {
		// Do nothing
	}
}

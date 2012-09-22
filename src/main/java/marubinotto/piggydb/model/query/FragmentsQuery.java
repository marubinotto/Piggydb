package marubinotto.piggydb.model.query;

import marubinotto.piggydb.model.Fragment;
import marubinotto.piggydb.model.base.Query;

public interface FragmentsQuery extends Query<Fragment> {
	
	public void setSortOption(FragmentsSortOption sortOption);
	
	public void setEagerFetching(boolean eagerFetching);
}

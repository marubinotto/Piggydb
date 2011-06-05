package marubinotto.piggydb.model.repository;

import marubinotto.piggydb.model.Filter;
import marubinotto.piggydb.model.FilterRepository;
import marubinotto.piggydb.model.User;
import marubinotto.piggydb.model.entity.RawFilter;
import marubinotto.util.Assert;

public abstract class AbstractFilterRepository 
extends AbstractRepository<Filter, RawFilter> implements FilterRepository {

	public RawFilter newRawEntity() {
		return new RawFilter();
	}

	public Filter newInstance(User user) {
		Assert.Arg.notNull(user, "user");
		return new RawFilter(user);
	}
}

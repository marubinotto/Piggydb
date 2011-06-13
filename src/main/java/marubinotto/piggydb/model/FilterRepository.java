package marubinotto.piggydb.model;

import java.util.List;

import marubinotto.piggydb.model.entity.RawFilter;
import marubinotto.util.Assert;
import marubinotto.util.paging.Page;

public interface FilterRepository extends Repository<Filter> {
	
	public Filter newInstance(User user);
	
	public TagRepository getTagRepository();

	public Filter getByName(String name) throws Exception;
	
	public Long getIdByName(String name) throws Exception;
	
	public List<String> getNamesLike(String criteria) throws Exception;
	
	// With only an ID and name, date
	public Page<Filter> getRecentChanges(int pageSize, int pageIndex)
	throws Exception;
	
	
	public static abstract class Base
	extends Repository.Base<Filter, RawFilter> implements FilterRepository {
		
		public RawFilter newRawEntity() {
			return new RawFilter();
		}

		public Filter newInstance(User user) {
			Assert.Arg.notNull(user, "user");
			return new RawFilter(user);
		}
	}
}

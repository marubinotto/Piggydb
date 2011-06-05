package marubinotto.piggydb.model;

import java.util.List;

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
}

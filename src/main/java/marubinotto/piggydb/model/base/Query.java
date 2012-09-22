package marubinotto.piggydb.model.base;

import java.util.List;

import marubinotto.util.paging.Page;

public interface Query<E extends Entity> {

	public void setRepository(Repository<E> repository);
	
	public abstract List<E> getAll() throws Exception;
	
	public abstract Page<E> getPage(int pageSize, int pageIndex)
	throws Exception;
}

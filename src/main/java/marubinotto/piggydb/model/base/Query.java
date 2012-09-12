package marubinotto.piggydb.model.base;

import java.util.List;

import marubinotto.util.paging.Page;

public abstract class Query<E extends Entity, R extends Repository<E>> {

	protected R repository;
	
	public void setRepository(R repository) {
		this.repository = repository;
	}
	
	public abstract List<E> getAll() throws Exception;
	
	public abstract Page<E> getPage(int pageSize, int pageIndex);
}

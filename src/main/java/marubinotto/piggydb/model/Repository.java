package marubinotto.piggydb.model;

import java.util.Map;
import java.util.Set;

public interface Repository<E extends Entity> {

	public long size() throws Exception;

	public long register(E entity) throws Exception;
	
	public E get(long id) throws Exception;
	
	public boolean update(E entity) 
	throws BaseDataObsoleteException, Exception;
	
	public void delete(long id, User user) throws Exception;
	
	public Map<Long, String> getNames(Set<Long> ids) throws Exception;
}

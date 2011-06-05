package marubinotto.piggydb.model.repository;

import marubinotto.piggydb.model.Entity;
import marubinotto.piggydb.model.Repository;
import marubinotto.piggydb.model.User;
import marubinotto.piggydb.model.entity.RawEntity;
import marubinotto.util.Assert;

public abstract class AbstractRepository<E extends Entity, R extends RawEntity> 
implements Repository<E>, RawEntityFactory<R> {

	public void delete(long id, User user) throws Exception {
		Assert.Arg.notNull(user, "user");
		
		E entity = get(id);
		if (entity == null) return;
		entity.ensureCanDelete(user);
		doDelete(entity, user);
	}
	
	protected abstract void doDelete(E entity, User user) throws Exception;
}

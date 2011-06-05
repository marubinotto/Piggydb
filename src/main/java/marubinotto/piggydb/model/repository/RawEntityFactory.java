package marubinotto.piggydb.model.repository;

import marubinotto.piggydb.model.entity.RawEntity;

public interface RawEntityFactory<T extends RawEntity> {

	public T newRawEntity();
}

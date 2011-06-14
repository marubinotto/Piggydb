package marubinotto.piggydb.model.entity;

public interface RawEntityFactory<T extends RawEntity> {

	public T newRawEntity();
}

package marubinotto.piggydb.model.repository;

import marubinotto.piggydb.model.Tag;
import marubinotto.piggydb.model.TagRepository;
import marubinotto.piggydb.model.User;
import marubinotto.piggydb.model.entity.RawTag;
import marubinotto.util.Assert;

public abstract class AbstractTagRepository 
extends AbstractRepository<Tag, RawTag> implements TagRepository {

	public RawTag newRawEntity() {
		return new RawTag();
	}

	public Tag newInstance(String name, User user) {
		Assert.Arg.notNull(user, "user");
		return new RawTag(name, user);
	}

	public Tag getTrashTag() throws Exception {
		return getByName(Tag.NAME_TRASH);
	}
}

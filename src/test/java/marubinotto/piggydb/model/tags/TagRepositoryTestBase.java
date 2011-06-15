package marubinotto.piggydb.model.tags;

import java.util.List;

import marubinotto.piggydb.impl.jdbc.h2.InMemoryDatabase;
import marubinotto.piggydb.model.RepositoryTestBase;
import marubinotto.piggydb.model.Tag;
import marubinotto.piggydb.model.TagRepository;

import org.junit.runners.Parameterized.Parameters;

public abstract class TagRepositoryTestBase 
extends RepositoryTestBase<TagRepository> {
	
	public TagRepositoryTestBase(
			RepositoryFactory<TagRepository> factory) {
		super(factory);
	}
	
	@Parameters
	public static List<Object[]> factories() {
		return toParameters(
			new RepositoryFactory<TagRepository>() {
				public TagRepository create() throws Exception {
					return new InMemoryDatabase().getTagRepository();
				}
			}
		);
	}
	
	public Tag newTag(String name) {
		return this.object.newInstance(name, getPlainUser());
	}
	
	public Tag newTagWithTags(String tagName, String ... parentTags) 
	throws Exception {
		Tag newTag = newTag(tagName);
		for (String parent : parentTags) {
			newTag.addTagByUser(parent, this.object, getPlainUser());
		}
		return newTag;
	}
}

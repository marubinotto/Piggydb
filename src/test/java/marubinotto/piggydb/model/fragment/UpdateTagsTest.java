package marubinotto.piggydb.model.fragment;

import static marubinotto.piggydb.fixture.EntityFixtures.newTagWithTags;
import static marubinotto.piggydb.model.Assert.assertClassificationEquals;
import static marubinotto.util.CollectionUtils.list;
import static marubinotto.util.CollectionUtils.set;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import marubinotto.piggydb.impl.jdbc.h2.InMemoryDatabase;
import marubinotto.piggydb.model.InvalidTaggingException;
import marubinotto.piggydb.model.TagRepository;
import marubinotto.piggydb.model.User;
import marubinotto.piggydb.model.entity.RawFragment;

import org.junit.Before;
import org.junit.Test;

public class UpdateTagsTest {

	private RawFragment object = new RawFragment();
	
	private User plainUser = new User("plain");
	private TagRepository tagRepository = new InMemoryDatabase().getTagRepository();
	
	@Before
	public void given() throws Exception {
		this.tagRepository.register(newTagWithTags("child", "parent"));
	}
	
	private void execute(List<String> originalTags, List<String> newTags, Set<String> expectation) 
	throws InvalidTaggingException, Exception {
		if (originalTags != null) {
			for (String tag : originalTags) {
				this.object.addTagByUser(tag, this.tagRepository, this.plainUser);
			}
		}
		this.object.updateTagsByUser(newTags, this.tagRepository, this.plainUser);
		assertClassificationEquals(expectation,  this.object.getClassification());
	}
	
	@Test
	public void withEmpty() throws Exception {
		execute(null, new ArrayList<String>(), new HashSet<String>());
	}
	
	@Test
	public void addOne() throws Exception {
		execute(null, list("idiot"), set("idiot"));
	}
	
	@Test
	public void replaceOne() throws Exception {
		execute(list("idiot"), list("genious"), set("genious"));
	}
	
	@Test
	public void appendOne() throws Exception {
		execute(list("idiot"), list("idiot", "genious"), set("idiot", "genious"));
	}
	
	@Test
	public void removeOne() throws Exception {
		execute(list("idiot"), new ArrayList<String>(), new HashSet<String>());
	}
	
	@Test
	public void selectMostConcreteTag_case1() throws Exception {
		execute(null, list("child", "parent"), set("child"));
	}
	
	@Test
	public void selectMostConcreteTag_case2() throws Exception {
		execute(null, list("parent", "child"), set("child"));
	}
	
	@Test
	public void selectMostConcreteTag_case3() throws Exception {
		execute(list("child"), list("parent", "child"), set("child"));
	}
	
	@Test
	public void selectMostConcreteTag_case4() throws Exception {
		execute(list("child"), list("child", "parent"), set("child"));
	}
	
	@Test
	public void selectMostConcreteTag_case5() throws Exception {
		execute(list("parent"), list("parent", "child"), set("child"));
	}
	
	@Test
	public void selectMostConcreteTag_case6() throws Exception {
		execute(list("parent"), list("child", "parent"), set("child"));
	}
}

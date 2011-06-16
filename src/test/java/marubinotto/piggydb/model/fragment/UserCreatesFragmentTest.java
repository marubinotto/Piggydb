package marubinotto.piggydb.model.fragment;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNull;
import static marubinotto.piggydb.model.Assert.assertClassificationEquals;
import static marubinotto.util.CollectionUtils.list;
import static marubinotto.util.CollectionUtils.set;
import marubinotto.piggydb.impl.InMemoryDatabase;
import marubinotto.piggydb.model.TagRepository;
import marubinotto.piggydb.model.User;
import marubinotto.piggydb.model.entity.RawFragment;
import marubinotto.piggydb.model.entity.RawTag;

import org.junit.Before;
import org.junit.Test;

public class UserCreatesFragmentTest {

	private RawFragment newFragment = new RawFragment(new User("daisuke"));
	private TagRepository tagRepository = new InMemoryDatabase().getTagRepository();
	
	@Before
	public void given() throws Exception {
		assertNull(this.newFragment.getId());  // indicating being not yet registered
	}
	
	@Test
	public void creator() throws Exception {
		assertEquals("daisuke", this.newFragment.getCreator());
	}
	
	@Test
	public void updater() throws Exception {
		assertNull(this.newFragment.getUpdater());
	}
	
	private void assertCreatorUnchanged() {
		assertEquals("daisuke", this.newFragment.getCreator());
	}
	
	@Test
	public void setTitle() throws Exception {
		this.newFragment.setTitleByUser("title", new User("akane"));
		
		assertEquals("title", this.newFragment.getTitle());
		assertCreatorUnchanged();
		assertNull(this.newFragment.getUpdater());
	}
	
	@Test
	public void setContent() throws Exception {
		this.newFragment.setContentByUser("content", new User("akane"));
		
		assertEquals("content", this.newFragment.getContent());
		assertCreatorUnchanged();
		assertNull(this.newFragment.getUpdater());
	}
	
	@Test
	public void addTag() throws Exception {
		this.newFragment.addTagByUser(new RawTag("tag"), new User("akane"));
		
		assertClassificationEquals(set("tag"),  this.newFragment.getClassification());
		assertCreatorUnchanged();
		assertNull(this.newFragment.getUpdater());
	}
	
	@Test
	public void addTagByName() throws Exception {
		this.newFragment.addTagByUser("tag", this.tagRepository, new User("akane"));
		
		assertClassificationEquals(set("tag"),  this.newFragment.getClassification());
		assertCreatorUnchanged();
		assertNull(this.newFragment.getUpdater());
	}
	
	@Test
	public void updateTags() throws Exception {
		this.newFragment.updateTagsByUser(list("foo", "bar"), this.tagRepository, new User("akane"));
		
		assertClassificationEquals(set("foo", "bar"),  this.newFragment.getClassification());
		assertCreatorUnchanged();
		assertNull(this.newFragment.getUpdater());
	}
	
	@Test
	public void removeTag() throws Exception {
		this.newFragment.updateTagsByUser(list("foo", "bar"), this.tagRepository, new User("akane"));
		
		this.newFragment.removeTagByUser("foo", new User("daisuke"));
		
		assertClassificationEquals(set("bar"),  this.newFragment.getClassification());
		assertCreatorUnchanged();
		assertNull(this.newFragment.getUpdater());
	}
}

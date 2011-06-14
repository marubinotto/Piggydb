package marubinotto.piggydb.model.fragment;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNull;
import static marubinotto.piggydb.model.Assert.assertClassificationEquals;
import static marubinotto.util.CollectionUtils.list;
import static marubinotto.util.CollectionUtils.set;
import marubinotto.piggydb.external.jdbc.h2.InMemoryDatabase;
import marubinotto.piggydb.model.TagRepository;
import marubinotto.piggydb.model.User;
import marubinotto.piggydb.model.entity.RawFragment;
import marubinotto.piggydb.model.entity.RawTag;

import org.junit.Before;
import org.junit.Test;

public class UserUpdatesFragmentTest {

	private RawFragment object = new RawFragment(new User("daisuke"));
	private TagRepository tagRepository = new InMemoryDatabase().getTagRepository();
	
	@Before
	public void given() throws Exception {
		this.object.setId(1L);		// indicating being stored
	}
	
	@Test
	public void touch() throws Exception {
		this.object.touch(new User("akane"), true);
		
		assertEquals("akane", this.object.getUpdater());
	}
	
	@Test
	public void setTitle() throws Exception {
		this.object.setTitleByUser("title", new User("akane"));
		
		assertEquals("title", this.object.getTitle());
		assertEquals("akane", this.object.getUpdater());
	}
	
	@Test
	public void setSameTitle() throws Exception {
		this.object.setTitleByUser(null, new User("akane"));
		
		assertNull(this.object.getTitle());
		assertEquals("akane", this.object.getUpdater());
	}

	@Test
	public void setContent() throws Exception {
		this.object.setContentByUser("content", new User("akane"));
		
		assertEquals("content", this.object.getContent());
		assertEquals("akane", this.object.getUpdater());
	}
	
	@Test
	public void setSameContent() throws Exception {
		this.object.setContentByUser(null, new User("akane"));
		
		assertNull(this.object.getContent());
		assertEquals("akane", this.object.getUpdater());
	}
	
	@Test
	public void addTag() throws Exception {
		this.object.addTagByUser(new RawTag("tag"), new User("akane"));
		
		assertClassificationEquals(set("tag"),  this.object.getClassification());
		assertEquals("akane", this.object.getUpdater());
	}
	
	@Test
	public void addTagByName() throws Exception {
		this.object.addTagByUser("tag", this.tagRepository, new User("akane"));
		
		assertClassificationEquals(set("tag"),  this.object.getClassification());
		assertEquals("akane", this.object.getUpdater());
	}
	
	@Test
	public void updateTags() throws Exception {
		this.object.updateTagsByUser(list("foo", "bar"), this.tagRepository, new User("akane"));
		
		assertClassificationEquals(set("foo", "bar"),  this.object.getClassification());
		assertEquals("akane", this.object.getUpdater());
	}
	
	@Test
	public void removeTag() throws Exception {
		this.object.updateTagsByUser(list("foo", "bar"), this.tagRepository, new User("akane"));
		
		this.object.removeTagByUser("foo", new User("daisuke"));
		
		assertClassificationEquals(set("bar"),  this.object.getClassification());
		assertEquals("daisuke", this.object.getUpdater());
	}
}

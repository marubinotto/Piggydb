package marubinotto.piggydb.model.tags;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.Iterator;

import marubinotto.piggydb.model.Tag;
import marubinotto.piggydb.model.TagRepository;
import marubinotto.piggydb.model.auth.User;
import marubinotto.piggydb.model.exception.BaseDataObsoleteException;
import marubinotto.piggydb.model.exception.DuplicateException;
import marubinotto.util.time.DateTime;

import org.junit.Before;
import org.junit.Test;

public class OneTagTest extends TagRepositoryTestBase {

	protected long id;
	private DateTime registerDateTime = new DateTime(2008, 1, 1);
	
	public OneTagTest(RepositoryFactory<TagRepository> factory) {
		super(factory);
	}

	@Before
	public void given() throws Exception {
		super.given();
		
		DateTime.setCurrentTimeForTest(registerDateTime);
		
		Tag newTag = this.object.newInstance("tag", new User("daisuke"));
		this.id = this.object.register(newTag);
		
		DateTime.setCurrentTimeForTest(null);
	}
	
	@Test
	public void sizeShouldBeOne() throws Exception {
		assertEquals(1, this.object.size());
	}
	
	@Test
	public void getById() throws Exception {
		Tag tag = this.object.get(this.id);
		
		assertNotNull(tag);
		assertEquals(new Long(this.id), tag.getId());
		assertEquals("tag", tag.getName());
		assertEquals(this.registerDateTime, tag.getCreationDatetime());
		assertEquals(this.registerDateTime, tag.getUpdateDatetime());
		assertEquals("daisuke", tag.getCreator());
		assertNull(tag.getUpdater());
	}
	
	@Test
	public void getByNonexistentId() throws Exception {
		assertNull(this.object.get(1234));
	}

	@Test
	public void getByName() throws Exception {
		Tag tag = this.object.getByName("tag");
		
		assertNotNull(tag);
		assertEquals(new Long(this.id), tag.getId());
		assertEquals("tag", tag.getName());
	}
	
	@Test
	public void getByNonexistentName() throws Exception {
		assertNull(this.object.getByName("no-such-tag"));
	}
	
	@Test
	public void containsName() throws Exception {
		assertTrue(this.object.containsName("tag"));
		assertFalse(this.object.containsName("no-such-tag"));
	}
	
	@Test
	public void getIdByName() throws Exception {
		Long result = this.object.getIdByName("tag");
		assertEquals(this.id, result.longValue());
	}
	
	@Test
	public void getIdByNonexistentName() throws Exception {
		assertNull(this.object.getIdByName("no-such-tag"));
	}

	@Test
	public void iterateOneTagName() throws Exception {
		Iterator<String> tagNames = this.object.iterateAllTagNames();
		assertEquals("tag", tagNames.next());
		assertFalse(tagNames.hasNext());
	}

	
	@Test
	public void update() throws Exception {
		// Given
		DateTime updateDateTime = new DateTime(2008, 1, 2);
		DateTime.setCurrentTimeForTest(updateDateTime);

		// When
		Tag toUpdate = this.object.get(this.id);
		toUpdate.setNameByUser("tag-updated", new User("akane"));
		boolean result = this.object.update(toUpdate);
		
		// Then
		assertTrue(result);
		assertEquals(updateDateTime, toUpdate.getUpdateDatetime());
		
		assertFalse(this.object.containsName("tag"));
		assertNull(this.object.getByName("tag"));		
		assertTrue(this.object.containsName("tag-updated"));
		assertNotNull(this.object.getByName("tag-updated"));
		
		Tag retrieved = this.object.get(this.id);
		assertEquals(updateDateTime, retrieved.getUpdateDatetime());
		assertEquals("tag-updated", retrieved.getName());
		assertEquals("daisuke", retrieved.getCreator());
		assertEquals("akane", retrieved.getUpdater());
	}
	
	@Test
	public void updateNonexistentTag() throws Exception {
		Tag tag = this.object.get(this.id);
		this.object.delete(this.id, getPlainUser());

		boolean result = this.object.update(tag);
		
		assertFalse(result);		
	}
	
	@Test(expected=BaseDataObsoleteException.class)
	public void baseDataAlreadyUpdated() throws Exception {
		// Given
		Tag baseData1 = this.object.get(this.id);
		
		Tag baseData2 = this.object.get(this.id);
		baseData2.setNameByUser("tag-updated-first", new User("akane"));
		this.object.update(baseData2);
		
		// When
		baseData1.setNameByUser("tag-updated-second", new User("daisuke"));
		this.object.update(baseData1);
	}
	
	@Test
	public void modifyReturnedTagWithoutUpdating() throws Exception {
		// When
		Tag tag = this.object.get(this.id);
		tag.setNameByUser("tag-updated", getPlainUser());
		
		// Then
		assertEquals("tag", this.object.get(this.id).getName());
	}
	
	@Test
	public void modifyReturnedTagByNameWithoutUpdating() throws Exception {
		// When
		Tag tag = this.object.getByName("tag");
		tag.setNameByUser("tag-updated", getPlainUser());
		
		// Then
		assertEquals("tag", this.object.get(this.id).getName());
	}
	
	@Test
	public void registerAnotherTag() throws Exception {
		// When
		Tag anotherTag = this.object.newInstance("another", getPlainUser());
		long anotherTagId = this.object.register(anotherTag);
		
		// Then
		assertEquals(anotherTagId, anotherTag.getId().longValue());	
		
		Tag tag = this.object.get(anotherTagId);	
		assertEquals(anotherTagId, tag.getId().longValue());
		assertEquals("another", tag.getName());
	}
	
	@Test(expected=DuplicateException.class)
	public void registerDuplicateName() throws Exception {
		this.object.register(newTag("tag"));
	}
	
	@Test(expected=DuplicateException.class)
	public void updateToDuplicateName() throws Exception {
		// Given
		this.object.register(newTag("another"));

		// When
		Tag baseData = this.object.get(this.id);
		baseData.setNameByUser("another", getPlainUser());
		this.object.update(baseData);
	}
	
	@Test
	public void delete() throws Exception {
		// When
		this.object.delete(this.id, getPlainUser());
		
		// Then
		assertEquals(0, this.object.size());
		assertNull(this.object.get(this.id));
		assertNull(this.object.getByName("tag"));
		assertFalse(this.object.containsName("tag"));
	}
}

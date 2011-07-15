package marubinotto.piggydb.model.fragments;

import static marubinotto.util.time.DateTime.setCurrentTimeForTest;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import marubinotto.piggydb.model.Fragment;
import marubinotto.piggydb.model.FragmentRepository;
import marubinotto.piggydb.model.User;
import marubinotto.piggydb.model.exception.BaseDataObsoleteException;
import marubinotto.util.time.DateTime;

import org.junit.Before;
import org.junit.Test;

public class OneFragmentTest extends FragmentRepositoryTestBase {
	
	private long id;
	private DateTime registrationTime;
	
	public OneFragmentTest(RepositoryFactory<FragmentRepository> factory) {
		super(factory);
	}

	@Before
	public void given() throws Exception {
		super.given();
		
		this.registrationTime = new DateTime(2008, 1, 1);
		setCurrentTimeForTest(this.registrationTime);
		
		User user = new User("daisuke");
		Fragment fragment = this.object.newInstance(user);
		fragment.setTitleByUser("title", user);
		fragment.setContentByUser("content", user);
		fragment.changePassword("password");
		
		this.id = this.object.register(fragment);	
		
		setCurrentTimeForTest(null);
	}
	
	@Test
	public void sizeShouldBeOne() throws Exception {
		assertEquals(1, this.object.size());
	}
	
	@Test
	public void getById() throws Exception {
		Fragment retrieved = this.object.get(this.id);
		
		// Attributes
		assertEquals(this.id, retrieved.getId().longValue());
		assertEquals("title", retrieved.getTitle());
		assertEquals("content", retrieved.getContent());
		assertEquals(this.registrationTime, retrieved.getCreationDatetime());
		assertEquals(this.registrationTime, retrieved.getUpdateDatetime());
		assertTrue(retrieved.validatePassword("password"));
		assertEquals("daisuke", retrieved.getCreator());
		assertNull(retrieved.getUpdater());
		
		// Dependencies
		assertTrue(retrieved.getClassification().isEmpty());
		assertTrue(retrieved.getParentRelations().isEmpty());
		assertFalse(retrieved.hasChildren());
		assertTrue(retrieved.getChildRelations().isEmpty());
	}

	@Test
	public void getByNonexistentId() throws Exception {
		assertNull(this.object.get(123));
	}

	@Test
	public void update() throws Exception {
		// Given
		DateTime updateTime = new DateTime(2008, 1, 2);
		DateTime.setCurrentTimeForTest(updateTime);
		
		// When
		User user = new User("akane");
		Fragment forUpdate = this.object.get(this.id);
		forUpdate.setTitleByUser("modified-title", user);
		forUpdate.setContentByUser("modified-content", user);
		forUpdate.changePassword("changed-password");
		
		boolean result = this.object.update(forUpdate);
		
		// Then
		assertTrue(result);
		assertEquals(updateTime, forUpdate.getUpdateDatetime());
		
		Fragment retrieved = this.object.get(this.id);
		assertEquals("modified-title", retrieved.getTitle());
		assertEquals("modified-content", retrieved.getContent());
		assertEquals(updateTime, retrieved.getUpdateDatetime());
		assertTrue(retrieved.validatePassword("changed-password"));
		assertEquals("daisuke", retrieved.getCreator());
		assertEquals("akane", retrieved.getUpdater());
	}
	
	@Test
	public void updateWithoutTimestamp() throws Exception {
		// Given
		DateTime updateTime = new DateTime(2008, 1, 2);
		DateTime.setCurrentTimeForTest(updateTime);
		
		// When
		User user = new User("akane");
		Fragment forUpdate = this.object.get(this.id);
		forUpdate.setTitleByUser("modified-title", user);
		
		boolean result = this.object.update(forUpdate, false);
		
		// Then
		assertTrue(result);
		assertEquals(this.registrationTime, forUpdate.getUpdateDatetime());
		
		Fragment retrieved = this.object.get(this.id);
		assertEquals("modified-title", retrieved.getTitle());
		assertEquals(this.registrationTime, retrieved.getUpdateDatetime());
		assertEquals("akane", retrieved.getUpdater());
	}

	@Test
	public void updateNonexistentFragment() throws Exception {
		Fragment fragment = this.object.get(this.id);
		this.object.delete(this.id, getPlainUser());

		boolean result = this.object.update(fragment);
		
		assertFalse(result);
	}
	
	@Test(expected=BaseDataObsoleteException.class)
	public void updateWithBaseDataAlreadyUpdated() throws Exception {
		// Given
		Fragment baseData1 = this.object.get(this.id);
		
		Fragment baseData2 = this.object.get(this.id);
		baseData2.setTitleByUser("modified-title", new User("akane"));
		this.object.update(baseData2);
		
		// When
		baseData1.setTitleByUser("cannot-store-this", new User("daisuke"));
		this.object.update(baseData1);
	}
	
	@Test
	public void modifyRetrievedFragmentButNotUpdating() throws Exception {
		// When
		Fragment fragment1 = this.object.get(this.id);
		fragment1.setTitleByUser("modified-title", new User("akane"));
		
		// Then
		Fragment fragment2 = this.object.get(this.id);
		assertEquals("title", fragment2.getTitle());
		assertNull(fragment2.getUpdater());
	}
	
	@Test
	public void delete() throws Exception {
		// When
		this.object.delete(this.id, getPlainUser());
		
		// Then
		assertEquals(0, this.object.size());
		assertNull(this.object.get(this.id));
	}
}

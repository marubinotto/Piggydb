package marubinotto.piggydb.model.fragments;

import static marubinotto.util.time.DateTime.setCurrentTimeForTest;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import marubinotto.piggydb.model.Fragment;
import marubinotto.piggydb.model.FragmentRepository;
import marubinotto.piggydb.model.FragmentsOptions;
import marubinotto.piggydb.model.User;
import marubinotto.util.paging.Page;
import marubinotto.util.time.DateTime;

import org.junit.Test;

public class DefaultTest extends FragmentRepositoryTestBase {
	
	public DefaultTest(RepositoryFactory<FragmentRepository> factory) {
		super(factory);
	}

	@Test
	public void sizeShouldBeZero() throws Exception {
		assertEquals(0, this.object.size());
	}
	
	@Test
	public void relationCountShouldBeZero() throws Exception {
		assertEquals(0, this.object.countRelations().longValue());
	}
	
	@Test
	public void newInstance() throws Exception {
		User user = getPlainUser();
		Fragment fragment = this.object.newInstance(user);
		
		assertEquals(user.getName(), fragment.getCreator());
		assertNull(fragment.getUpdater());
	}
	
	@Test
	public void register() throws Exception {
		// Given
		DateTime registrationTime = DateTime.getCurrentTime();
		setCurrentTimeForTest(registrationTime);
		
		// When
		Fragment fragment = this.object.newInstance(getPlainUser());
		Long newId = this.object.register(fragment);
		
		// Then
		assertEquals("The ID should start with 1", 1, newId.intValue());
		assertEquals(newId, fragment.getId());
		assertEquals(registrationTime, fragment.getCreationDatetime());
		assertEquals(registrationTime, fragment.getUpdateDatetime());
		
		// NOTE: The post conditions of the repository is described by OneFragmentTest
	}
	
	@Test
	public void fragmentsShoudBeEmpty() throws Exception {
		Page<Fragment> results = this.object.getFragments(
			new FragmentsOptions(1, 0, false));
		assertTrue(results.isEmpty());
	}
	
	@Test
	public void modifyOriginalObjectAfterRegistration() throws Exception {
		// Given
		User user = getPlainUser();
		Fragment original = this.object.newInstance(user);
		original.setTitleByUser("title", user);
		original.setContentByUser("content", user);
		long id = this.object.register(original);
		
		// When
		original.setTitleByUser("modified-title", user);
		original.setContentByUser("modified-content", user);
		original.addTagByUser("tag", this.object.getTagRepository(), user);
	
		// Then
		Fragment retrieved = this.object.get(id);
		assertEquals("title", retrieved.getTitle());
		assertEquals("content", retrieved.getContent());
		assertEquals(0, retrieved.getClassification().size());
	}
}

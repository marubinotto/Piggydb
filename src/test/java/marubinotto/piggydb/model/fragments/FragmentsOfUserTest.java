package marubinotto.piggydb.model.fragments;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNull;
import static marubinotto.util.time.DateTime.setCurrentTimeForTest;
import marubinotto.piggydb.model.Fragment;
import marubinotto.piggydb.model.FragmentRepository;
import marubinotto.piggydb.model.Tag;
import marubinotto.piggydb.model.auth.User;
import marubinotto.piggydb.model.query.FragmentsOfUser;

import org.junit.Test;

public class FragmentsOfUserTest extends FragmentRepositoryTestBase {

	public FragmentsOfUserTest(RepositoryFactory<FragmentRepository> factory) {
		super(factory);
	}

	public Fragment newUserFragment(String userName) throws Exception {
		User user = getOwner();
		Fragment fragment = this.object.newInstance(user);
		fragment.setTitleByUser(userName, user);
		fragment.addTagByUser(Tag.NAME_USER, this.object.getTagRepository(), user);
		return fragment;
	}
	
	private FragmentsOfUser getQuery(String userName) throws Exception {
		FragmentsOfUser query = (FragmentsOfUser)this.object.getQuery(FragmentsOfUser.class);
		query.setUserName(userName);
		return query;
	}
	
	@Test
	public void empty() throws Exception {
		Fragment fragment = getQuery("marubinotto").getUserFragment();
		assertNull(fragment);
	}

	@Test
	public void oneUser() throws Exception {
		// Given
		Long id = this.object.register(newUserFragment("marubinotto"));
		
		// When
		Fragment fragment = getQuery("marubinotto").getUserFragment();
		
		// Then
		assertEquals(id, fragment.getId());
		assertEquals("marubinotto", fragment.getTitle());
	}
	
	@Test
	public void withoutUserTag() throws Exception {
		// Given
		this.object.register(newFragmentWithTitle("marubinotto"));
		
		// When
		Fragment fragment = getQuery("marubinotto").getUserFragment();
		
		// Then
		assertNull(fragment);
	}
	
	@Test
	public void userInTrash() throws Exception {
		// Given
		Fragment newFragment = newUserFragment("marubinotto");
		newFragment.addTagByUser(Tag.NAME_TRASH, this.object.getTagRepository(), getOwner());
		this.object.register(newFragment);
		
		// When
		Fragment fragment = getQuery("marubinotto").getUserFragment();
		
		// Then
		assertNull(fragment);
	}
	
	@Test
	public void noSuchFragment() throws Exception {
		// Given
		this.object.register(newUserFragment("akane"));
		
		// When
		Fragment fragment = getQuery("marubinotto").getUserFragment();
		
		// Then
		assertNull(fragment);
	}
	
	@Test
	public void duplicateUserFragment() throws Exception {
		// Given
		setCurrentTimeForTest(2008, 1, 1);
		this.object.register(newUserFragment("marubinotto"));
		
		setCurrentTimeForTest(2008, 1, 2);
		Long idOfNewOne = this.object.register(newUserFragment("marubinotto"));
		
		// When
		Fragment fragment = getQuery("marubinotto").getUserFragment();
		
		// Then
		assertEquals(idOfNewOne, fragment.getId());
		assertEquals("marubinotto", fragment.getTitle());
	}
}

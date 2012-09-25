package marubinotto.piggydb.model.fragments;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertTrue;
import static marubinotto.util.time.DateTime.setCurrentTimeForTest;
import marubinotto.piggydb.model.Fragment;
import marubinotto.piggydb.model.FragmentRepository;
import marubinotto.piggydb.model.Tag;
import marubinotto.piggydb.model.auth.User;
import marubinotto.piggydb.model.query.FragmentsByUser;
import marubinotto.util.paging.Page;

import org.junit.Before;
import org.junit.Test;

public class FragmentsByUserTest extends FragmentRepositoryTestBase {

	public FragmentsByUserTest(RepositoryFactory<FragmentRepository> factory) {
		super(factory);
	}
	
	private Long id1;
	private Long id2;
	
	@Before
	public void given() throws Exception {
		super.given();
		
		setCurrentTimeForTest(2008, 1, 1);
		this.id1 = this.object.register(this.object.newInstance(new User("creator")));
		this.id2 = this.object.register(this.object.newInstance(new User("creator-and-updater")));
		
		setCurrentTimeForTest(2008, 1, 2);
		Fragment forUpdate = this.object.get(this.id1);
		forUpdate.setContentByUser("modified", new User("creator-and-updater"));
		this.object.update(forUpdate);
		
		setCurrentTimeForTest(2008, 1, 3);
		forUpdate = this.object.get(this.id2);
		forUpdate.setContentByUser("modified", new User("updater"));
		this.object.update(forUpdate);
		
		setCurrentTimeForTest(null);
	}
	
	private FragmentsByUser getQuery() throws Exception {
		return (FragmentsByUser)this.object.getQuery(FragmentsByUser.class);
	}
	
	@Test
	public void noSuchUser() throws Exception {
		FragmentsByUser query = getQuery();
		query.setUserName("no-such-user");
		Page<Fragment> results = query.getPage(5, 0);
		
		assertTrue(results.isEmpty());
	}
	
	@Test
	public void asCreator() throws Exception {
		FragmentsByUser query = getQuery();
		query.setUserName("creator");
		Page<Fragment> results = query.getPage(5, 0);
		
		assertEquals(1, results.size());
		assertEquals(this.id1, results.get(0).getId());
	}
	
	@Test
	public void asUpdater() throws Exception {
		FragmentsByUser query = getQuery();
		query.setUserName("updater");
		Page<Fragment> results = query.getPage(5, 0);
		
		assertEquals(1, results.size());
		assertEquals(this.id2, results.get(0).getId());
	}
	
	@Test
	public void asBothCreatorAndUpdater() throws Exception {
		FragmentsByUser query = getQuery();
		query.setUserName("creator-and-updater");
		Page<Fragment> results = query.getPage(5, 0);
		
		assertEquals(2, results.size());
		assertEquals(this.id2, results.get(0).getId());
		assertEquals(this.id1, results.get(1).getId());
	}
	
	@Test
	public void taggedAsTrash() throws Exception {
		// Given
		Fragment forUpdate = this.object.get(this.id1);
		forUpdate.addTagByUser(
			Tag.NAME_TRASH, this.object.getTagRepository(), getPlainUser());
		this.object.update(forUpdate);
		
		// When
		FragmentsByUser query = getQuery();
		query.setUserName("creator");
		Page<Fragment> results = query.getPage(5, 0);
		
		// Then
		assertTrue(results.isEmpty());
	}
}

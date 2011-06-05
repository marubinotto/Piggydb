package marubinotto.piggydb.external.jdbc.h2;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertTrue;
import static marubinotto.util.time.DateTime.date;
import marubinotto.piggydb.external.jdbc.h2.H2FragmentRepository;
import marubinotto.piggydb.fixture.H2JdbcDaoFixtures;
import marubinotto.piggydb.model.Fragment;
import marubinotto.piggydb.model.FragmentsOptions;
import marubinotto.piggydb.model.OwnerAuth;
import marubinotto.util.paging.Page;

import org.junit.Before;
import org.junit.Test;

public class H2WhiteBoxTest {

	private H2JdbcDaoFixtures fixtures;
	
	private H2FragmentRepository fragmentRepository;
	
	@Before
	public void given() throws Exception {
		this.fixtures = new H2JdbcDaoFixtures();
		this.fragmentRepository = this.fixtures.createH2FragmentRepository();
	}
	
	@Test
	public void nullCreatorShouldBeRegardedAsOwner() throws Exception {
		this.fixtures.tables.fragment.cleanInsert(new Object[][]{
            {"fragment_id", "creation_datetime", "creator", "update_datetime", "updater"},
            {"1", date(2009, 1, 1), null, date(2009, 1, 1), null}
        });
		
		Page<Fragment> results = this.fragmentRepository.findByUser(
			OwnerAuth.USER_NAME_OWNER, 
			new FragmentsOptions(5, 0, false));
			
		assertEquals(1, results.size());
		assertEquals(1, results.get(0).getId().intValue());
		assertEquals(OwnerAuth.USER_NAME_OWNER, results.get(0).getCreator());
	}
	
	@Test
	public void nullUpdaterShouldBeRegardedAsOwnerWhenUpdated() throws Exception {
		this.fixtures.tables.fragment.cleanInsert(new Object[][]{
            {"fragment_id", "creation_datetime", "creator", "update_datetime", "updater"},
            {"1", date(2009, 1, 1), "someone", date(2009, 1, 2), null}
        });
		
		Page<Fragment> results = this.fragmentRepository.findByUser(
			OwnerAuth.USER_NAME_OWNER, 
			new FragmentsOptions(5, 0, false));
			
		assertEquals(1, results.size());
		assertEquals(1, results.get(0).getId().intValue());
		assertEquals(OwnerAuth.USER_NAME_OWNER, results.get(0).getUpdater());
	}
	
	@Test
	public void nullUpdaterShouldNotBeRegardedAsOwnerWhenNotUpdated() 
	throws Exception {
		this.fixtures.tables.fragment.cleanInsert(new Object[][]{
            {"fragment_id", "creation_datetime", "creator", "update_datetime", "updater"},
            {"1", date(2009, 1, 1), "someone", date(2009, 1, 1), null}
        });
		
		Page<Fragment> results = this.fragmentRepository.findByUser(
			OwnerAuth.USER_NAME_OWNER, 
			new FragmentsOptions(5, 0, false));
		
		assertTrue(results.isEmpty());
	}
}

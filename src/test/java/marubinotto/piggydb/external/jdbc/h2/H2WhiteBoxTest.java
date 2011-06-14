package marubinotto.piggydb.external.jdbc.h2;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertTrue;
import static marubinotto.util.time.DateTime.date;
import marubinotto.piggydb.fixture.table.AllTables;
import marubinotto.piggydb.model.Fragment;
import marubinotto.piggydb.model.FragmentsOptions;
import marubinotto.piggydb.model.OwnerAuth;
import marubinotto.util.paging.Page;

import org.junit.Before;
import org.junit.Test;

public class H2WhiteBoxTest {

	private InMemoryDatabase database;
	private AllTables tables;
	private H2FragmentRepository fragmentRepository;

	@Before
	public void given() throws Exception {
		this.database = new InMemoryDatabase();
		this.tables = new AllTables(this.database.getDataSource());
		this.fragmentRepository = this.database.getFragmentRepository();
	}

	@Test
	public void nullCreatorShouldBeRegardedAsOwner() throws Exception {
		this.tables.fragment.cleanInsert(new Object[][]{
			{"fragment_id", "creation_datetime", "creator", "update_datetime", "updater"}, 
			{"1", date(2009, 1, 1), null, date(2009, 1, 1), null}});

		Page<Fragment> results = this.fragmentRepository.findByUser(
			OwnerAuth.USER_NAME_OWNER, new FragmentsOptions(5, 0, false));

		assertEquals(1, results.size());
		assertEquals(1, results.get(0).getId().intValue());
		assertEquals(OwnerAuth.USER_NAME_OWNER, results.get(0).getCreator());
	}

	@Test
	public void nullUpdaterShouldBeRegardedAsOwnerWhenUpdated() throws Exception {
		this.tables.fragment.cleanInsert(new Object[][]{
				{"fragment_id", "creation_datetime", "creator", "update_datetime", "updater"},
				{"1", date(2009, 1, 1), "someone", date(2009, 1, 2), null}});

		Page<Fragment> results = this.fragmentRepository.findByUser(
			OwnerAuth.USER_NAME_OWNER, new FragmentsOptions(5, 0, false));

		assertEquals(1, results.size());
		assertEquals(1, results.get(0).getId().intValue());
		assertEquals(OwnerAuth.USER_NAME_OWNER, results.get(0).getUpdater());
	}

	@Test
	public void nullUpdaterShouldNotBeRegardedAsOwnerWhenNotUpdated() throws Exception {
		this.tables.fragment.cleanInsert(new Object[][]{
				{"fragment_id", "creation_datetime", "creator", "update_datetime", "updater"},
				{"1", date(2009, 1, 1), "someone", date(2009, 1, 1), null}});

		Page<Fragment> results = this.fragmentRepository.findByUser(
			OwnerAuth.USER_NAME_OWNER, new FragmentsOptions(5, 0, false));

		assertTrue(results.isEmpty());
	}
}

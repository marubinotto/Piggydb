package marubinotto.piggydb.model.fragments;

import static marubinotto.util.time.DateTime.setCurrentTimeForTest;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import marubinotto.piggydb.model.Fragment;
import marubinotto.piggydb.model.FragmentRepository;
import marubinotto.piggydb.model.enums.FragmentField;
import marubinotto.piggydb.model.query.FragmentsByTime;
import marubinotto.util.paging.Page;
import marubinotto.util.time.DateTime;
import marubinotto.util.time.Month;

import org.junit.Before;
import org.junit.Test;

public class FragmentsByTimeTest extends FragmentRepositoryTestBase {

	protected long id1;
	protected long id2;
	protected long id3;
	
	public FragmentsByTimeTest(RepositoryFactory<FragmentRepository> factory) {
		super(factory);
	}

	@Before
	public void given() throws Exception {
		super.given();

		// Register
		
		setCurrentTimeForTest(2008, 1, 1);
		this.object.getHome(false, getOwner());
		this.id1  = this.object.register(newFragmentWithTitle("title1"));
		
		setCurrentTimeForTest(2008, 1, 2, 10, 0, 0);
		this.id2 = this.object.register(newFragmentWithTitle("title2"));
		
		setCurrentTimeForTest(2008, 1, 2, 15, 0, 0);
		this.id3 = this.object.register(newFragmentWithTitle("title3"));
		
		// Update
		
		setCurrentTimeForTest(2008, 1, 3);
		this.object.update(this.object.get(this.id1));
		
		setCurrentTimeForTest(2008, 1, 4, 10, 0, 0);
		this.object.update(this.object.get(this.id2));
		
		setCurrentTimeForTest(2008, 1, 4, 15, 0, 0);
		this.object.update(this.object.get(this.id3));
		
		setCurrentTimeForTest(null);
	}
	
	private FragmentsByTime getQuery() throws Exception {
		return (FragmentsByTime)this.object.getQuery(FragmentsByTime.class);
	}
	
	// By creation month
	
	@Test
	public void noCreationInSpecifiedMonth() throws Exception {
		FragmentsByTime query = getQuery();
		query.setCriteria(new Month(2008, 2), FragmentField.CREATION_DATETIME);
		Page<Fragment> page = query.getPage(5, 0);
		
		assertTrue(page.isEmpty());
	}
	
	@Test
	public void threeCreationsInSpecifiedMonth() throws Exception {
		FragmentsByTime query = getQuery();
		query.setCriteria(new Month(2008, 1), FragmentField.CREATION_DATETIME);
		Page<Fragment> page = query.getPage(5, 0);
		
		assertEquals(3, page.size());
		assertEquals("title3", page.get(0).getTitle());
		assertEquals("title2", page.get(1).getTitle());
		assertEquals("title1", page.get(2).getTitle());
	}
	
	// By creation day
	
	@Test
	public void noCreationOnSpecifiedDay() throws Exception {
		FragmentsByTime query = getQuery();
		query.setCriteria(
			new DateTime(2008, 1, 3).toDayInterval(), 
			FragmentField.CREATION_DATETIME);
		Page<Fragment> page = query.getPage(5, 0);
		
		assertTrue(page.isEmpty());
	}
	
	@Test
	public void oneCreationOnSpecifiedDay() throws Exception {
		FragmentsByTime query = getQuery();
		query.setCriteria(
			new DateTime(2008, 1, 1).toDayInterval(), 
			FragmentField.CREATION_DATETIME);
		Page<Fragment> page = query.getPage(5, 0);
		
		assertEquals(1, page.size());
		assertEquals("title1", page.get(0).getTitle());
	}
	
	@Test
	public void twoCreationsOnSpecifiedDay() throws Exception {
		FragmentsByTime query = getQuery();
		query.setCriteria(
			new DateTime(2008, 1, 2).toDayInterval(), 
			FragmentField.CREATION_DATETIME);
		Page<Fragment> page = query.getPage(5, 0);
		
		assertEquals(2, page.size());		
		assertEquals("title3", page.get(0).getTitle());
		assertEquals("title2", page.get(1).getTitle());
	}
	
	// By update day
	
	@Test
	public void noUpdateOnSpecifiedDay() throws Exception {
		FragmentsByTime query = getQuery();
		query.setCriteria(
			new DateTime(2007, 1, 1).toDayInterval(),
			FragmentField.UPDATE_DATETIME);
		Page<Fragment> page = query.getPage(5, 0);
		
		assertTrue(page.isEmpty());
	}

	@Test
	public void oneUpdateOnSpecifiedDay() throws Exception {
		FragmentsByTime query = getQuery();
		query.setCriteria(
			new DateTime(2008, 1, 3).toDayInterval(),
			FragmentField.UPDATE_DATETIME);
		Page<Fragment> page = query.getPage(5, 0);
		
		assertEquals(1, page.size());
		assertEquals("title1", page.get(0).getTitle());
	}

	@Test
	public void twoUpdatesOnSpecifiedDay() throws Exception {
		FragmentsByTime query = getQuery();
		query.setCriteria(
			new DateTime(2008, 1, 4).toDayInterval(),
			FragmentField.UPDATE_DATETIME);
		Page<Fragment> page = query.getPage(5, 0);
		
		assertEquals(2, page.size());		
		assertEquals("title3", page.get(0).getTitle());
		assertEquals("title2", page.get(1).getTitle());
	}
}

package marubinotto.piggydb.model.fragments;

import static marubinotto.util.CollectionUtils.set;
import static marubinotto.util.time.DateTime.setCurrentTimeForTest;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Set;

import marubinotto.piggydb.model.FragmentRepository;
import marubinotto.piggydb.model.enums.FragmentField;
import marubinotto.util.time.Month;

import org.junit.Before;
import org.junit.Test;

public class GetDaysOfMonthTest extends FragmentRepositoryTestBase {
	
	public GetDaysOfMonthTest(RepositoryFactory<FragmentRepository> factory) {
		super(factory);
	}

	@Before
	public void given() throws Exception {
		super.given();
		
		// Register
		setCurrentTimeForTest(2008, 1, 1);
		long id1 = this.object.register(newFragment());
		
		setCurrentTimeForTest(2008, 2, 5);
		long id2 = this.object.register(newFragment());
		
		setCurrentTimeForTest(2008, 2, 24);
		long id3 = this.object.register(newFragment());
		
		// Update
		
		setCurrentTimeForTest(2008, 3, 10);
		this.object.update(this.object.get(id1));
		
		setCurrentTimeForTest(2008, 4, 15);
		this.object.update(this.object.get(id2));
		
		setCurrentTimeForTest(2008, 4, 20);
		this.object.update(this.object.get(id3));
		
		setCurrentTimeForTest(null);
	}
	
	// Creation days
	
	@Test
	public void noCreationDays() throws Exception {
		Set<Integer> results = this.object.getDaysOfMonth(
			FragmentField.CREATION_DATETIME, new Month(2008, 3));
		assertTrue(results.isEmpty());
	}
	
	@Test
	public void oneCreationDay() throws Exception {
		Set<Integer> results = this.object.getDaysOfMonth(
			FragmentField.CREATION_DATETIME, new Month(2008, 1));		
		assertEquals(set(1), results);
	}
	
	@Test
	public void twoCreationDays() throws Exception {
		Set<Integer> results = this.object.getDaysOfMonth(
			FragmentField.CREATION_DATETIME, new Month(2008, 2));
		assertEquals(set(5, 24), results);
	}
	
	// Update days
	
	@Test
	public void noUpdateDays() throws Exception {
		Set<Integer> results = this.object.getDaysOfMonth(
			FragmentField.UPDATE_DATETIME, new Month(2008, 1));
		assertTrue(results.isEmpty());
	}
	
	@Test
	public void oneUpdateDay() throws Exception {
		Set<Integer> results = this.object.getDaysOfMonth(
			FragmentField.UPDATE_DATETIME, new Month(2008, 3));
		assertEquals(set(10), results);
	}
	
	@Test
	public void twoUpdateDays() throws Exception {
		Set<Integer> results = this.object.getDaysOfMonth(
			FragmentField.UPDATE_DATETIME, new Month(2008, 4));
		assertEquals(set(15, 20), results);
	}
}

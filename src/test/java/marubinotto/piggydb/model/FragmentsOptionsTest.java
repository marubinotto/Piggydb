package marubinotto.piggydb.model;

import static marubinotto.piggydb.fixture.EntityFixtures.newFragmentWithTitle;
import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.List;

import marubinotto.piggydb.model.enums.FragmentField;

import org.junit.Test;

public class FragmentsOptionsTest {

	private FragmentsOptions object = new FragmentsOptions();
	
	@Test
	public void defaultSortOption() throws Exception {
		assertEquals(FragmentField.UPDATE_DATETIME, this.object.sortOption.orderBy);
		assertEquals(false, this.object.sortOption.ascending);
	}
	
	@Test
	public void setNullToSortOption1() throws Exception {
		this.object.setSortOption((FragmentField)null, null);
		
		assertEquals(FragmentField.UPDATE_DATETIME, this.object.sortOption.orderBy);
		assertEquals(false, this.object.sortOption.ascending);
	}
	
	@Test
	public void setNullToSortOption2() throws Exception {
		this.object.setSortOption((Integer)null, null);
		
		assertEquals(FragmentField.UPDATE_DATETIME, this.object.sortOption.orderBy);
		assertEquals(false, this.object.sortOption.ascending);
	}
	
	@Test
	public void setSortOption() throws Exception {
		this.object.setSortOption(FragmentField.TITLE, true);
		
		assertEquals(FragmentField.TITLE, this.object.sortOption.orderBy);
		assertEquals(true, this.object.sortOption.ascending);
	}
	
	@Test
	public void sort() throws Exception {
		List<Fragment> fragments = new ArrayList<Fragment>();
		fragments.add(newFragmentWithTitle("b"));
		fragments.add(newFragmentWithTitle("a"));
		fragments.add(newFragmentWithTitle("c"));
		
		this.object.setSortOption(FragmentField.TITLE, true);
		this.object.sortOption.sort(fragments);
		
		assertEquals("a", fragments.get(0).getTitle());
		assertEquals("b", fragments.get(1).getTitle());
		assertEquals("c", fragments.get(2).getTitle());
	}
}

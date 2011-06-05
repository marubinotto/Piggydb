package marubinotto.piggydb.model.fragments;

import static marubinotto.util.CollectionUtils.set;
import static marubinotto.util.time.DateTime.setCurrentTimeForTest;
import static org.junit.Assert.assertEquals;

import java.util.List;

import marubinotto.piggydb.external.jdbc.h2.H2FragmentRepository;
import marubinotto.piggydb.model.Fragment;
import marubinotto.piggydb.model.FragmentRepository;
import marubinotto.piggydb.model.FragmentsOptions;
import marubinotto.piggydb.model.entity.RawFilter;
import marubinotto.piggydb.model.enums.FragmentField;
import marubinotto.util.paging.Page;
import marubinotto.util.time.Month;

import org.junit.Before;
import org.junit.Test;

public class SortFragmentsTest extends FragmentRepositoryTestBase {
	
	private Long id1;
	private Long id2;
	private Long id3;
	
	private FragmentsOptions defaultOptions = new FragmentsOptions();
	private FragmentsOptions orderByTitleAsc = new FragmentsOptions();

	public SortFragmentsTest(RepositoryFactory<FragmentRepository> factory) {
		super(factory);
	}
	
	@Before
	public void given() throws Exception {
		super.given();
		
		this.defaultOptions.setPagingOption(5, 0);
		this.orderByTitleAsc.setPagingOption(5, 0);
		this.orderByTitleAsc.setSortOption(FragmentField.TITLE, true);
		
		setCurrentTimeForTest(2008, 1, 1);
		this.id1 = this.object.register(
			newFragmentWithTitleAndContentAndTags("c", "hello", "tag"));
		
		setCurrentTimeForTest(2008, 1, 2);
		this.id2 = this.object.register(
			newFragmentWithTitleAndContentAndTags("a", "hello", "tag"));
		
		setCurrentTimeForTest(2008, 1, 3);
		this.id3 = this.object.register(
			newFragmentWithTitleAndContentAndTags("b", "hello", "tag"));
		
		setCurrentTimeForTest(null);
	}
	
	private void checkDefaultOrder(List<Fragment> fragments) {
		assertEquals("b", fragments.get(0).getTitle());
		assertEquals("a", fragments.get(1).getTitle());
		assertEquals("c", fragments.get(2).getTitle());
	}
	
	private void checkOrderByTitleAsc(List<Fragment> fragments) {
		assertEquals("a", fragments.get(0).getTitle());
		assertEquals("b", fragments.get(1).getTitle());
		assertEquals("c", fragments.get(2).getTitle());
	}
	
	@Test
	public void getFragments() throws Exception {
		checkDefaultOrder(this.object.getFragments(this.defaultOptions));
		checkOrderByTitleAsc(this.object.getFragments(this.orderByTitleAsc));
	}
	
	@Test
	public void findByTime() throws Exception {
		Month month = new Month(2008, 1);
		checkDefaultOrder(
			this.object.findByTime(
				month, FragmentField.CREATION_DATETIME, this.defaultOptions));
		checkOrderByTitleAsc(
			this.object.findByTime(
				month, FragmentField.CREATION_DATETIME, this.orderByTitleAsc));
	}
	
	@Test
	public void findByFilter() throws Exception {
		RawFilter filter = new RawFilter();
		filter.getClassification().addTag(storedTag("tag"));
		
		checkDefaultOrder(this.object.findByFilter(filter, this.defaultOptions));
		checkOrderByTitleAsc(this.object.findByFilter(filter, this.orderByTitleAsc));
	}
	
	@Test
	public void findByFilterWithPaging() throws Exception {
		RawFilter filter = new RawFilter();
		filter.getClassification().addTag(storedTag("tag"));
		
		FragmentsOptions options = new FragmentsOptions();
		options.setSortOption(FragmentField.TITLE, true);
		
		options.setPagingOption(1, 0);
		assertEquals("a", this.object.findByFilter(filter, options).get(0).getTitle());
		
		options.setPagingOption(1, 1);
		assertEquals("b", this.object.findByFilter(filter, options).get(0).getTitle());
		
		options.setPagingOption(1, 2);
		assertEquals("c", this.object.findByFilter(filter, options).get(0).getTitle());
	}
	
	@Test
	public void findByKeywords() throws Exception {
		if (this.object instanceof H2FragmentRepository) {
			checkDefaultOrder(this.object.findByKeywords("hello", this.defaultOptions));
			checkOrderByTitleAsc(this.object.findByKeywords("hello", this.orderByTitleAsc));
		}
	}
	
	@Test
	public void findByUser() throws Exception {
		checkDefaultOrder(
			this.object.findByUser(
				getPlainUser().getName(), this.defaultOptions));
		checkOrderByTitleAsc(
			this.object.findByUser(
				getPlainUser().getName(), this.orderByTitleAsc));
	}
	
	@Test
	public void getByIds() throws Exception {
		checkDefaultOrder(
			this.object.getByIds(
				set(this.id1, this.id2, this.id3), this.defaultOptions.sortOption, false));
		checkOrderByTitleAsc(
			this.object.getByIds(
				set(this.id1, this.id2, this.id3), this.orderByTitleAsc.sortOption, false));
	}
	
	@Test
	public void nullsLastIfAscending() throws Exception {
		this.object.register(newFragment());
		
		Page<Fragment> fragments = this.object.getFragments(this.orderByTitleAsc);
		
		assertEquals("a", fragments.get(0).getTitle());
		assertEquals("b", fragments.get(1).getTitle());
		assertEquals("c", fragments.get(2).getTitle());
		assertEquals(null, fragments.get(3).getTitle());
	}
	
	@Test
	public void nullsFirstIfDescending() throws Exception {
		this.object.register(newFragment());
		
		this.orderByTitleAsc.sortOption.ascending = false;
		Page<Fragment> fragments = this.object.getFragments(this.orderByTitleAsc);
		
		assertEquals(null, fragments.get(0).getTitle());	
		assertEquals("c", fragments.get(1).getTitle());
		assertEquals("b", fragments.get(2).getTitle());
		assertEquals("a", fragments.get(3).getTitle());
	}
	
	@Test
	public void ignoreCaseOfTitle() throws Exception {
		this.object.register(newFragmentWithTitle("B2"));
		
		Page<Fragment> fragments = this.object.getFragments(this.orderByTitleAsc);
		
		assertEquals("a", fragments.get(0).getTitle());
		assertEquals("b", fragments.get(1).getTitle());
		assertEquals("B2", fragments.get(2).getTitle());
		assertEquals("c", fragments.get(3).getTitle());
	}
}

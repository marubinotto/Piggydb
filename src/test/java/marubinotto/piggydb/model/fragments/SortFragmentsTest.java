package marubinotto.piggydb.model.fragments;

import static marubinotto.util.CollectionUtils.set;
import static marubinotto.util.time.DateTime.setCurrentTimeForTest;
import static org.junit.Assert.assertEquals;

import java.util.List;

import marubinotto.piggydb.impl.H2FragmentRepository;
import marubinotto.piggydb.model.Fragment;
import marubinotto.piggydb.model.FragmentRepository;
import marubinotto.piggydb.model.FragmentsOptions;
import marubinotto.piggydb.model.entity.RawFilter;
import marubinotto.piggydb.model.enums.FragmentField;
import marubinotto.piggydb.model.query.FragmentsAllButTrash;
import marubinotto.piggydb.model.query.FragmentsByFilter;
import marubinotto.piggydb.model.query.FragmentsByIds;
import marubinotto.piggydb.model.query.FragmentsByKeywords;
import marubinotto.piggydb.model.query.FragmentsByTime;
import marubinotto.piggydb.model.query.FragmentsByUser;
import marubinotto.piggydb.model.query.FragmentsQuery;
import marubinotto.piggydb.model.query.FragmentsSortOption;
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
	
	private Page<Fragment> queryAll(FragmentsOptions options) throws Exception {
		FragmentsQuery query = (FragmentsQuery)this.object.getQuery(FragmentsAllButTrash.class);
		query.setSortOption(options.sortOption);
		return query.getPage(options.pageSize, options.pageIndex);
	}
	
	private void assertSortingWorks(FragmentsQuery query) throws Exception {
		query.setSortOption(this.defaultOptions.sortOption);
		checkDefaultOrder(query.getPage(5, 0));
		
		query.setSortOption(this.orderByTitleAsc.sortOption);
		checkOrderByTitleAsc(query.getPage(5, 0));
	}
	
	@Test
	public void all() throws Exception {
		checkDefaultOrder(queryAll(this.defaultOptions));
		checkOrderByTitleAsc(queryAll(this.orderByTitleAsc));
	}
	
	@Test
	public void fragmentsByTime() throws Exception {
		FragmentsByTime query = (FragmentsByTime)this.object.getQuery(FragmentsByTime.class);
		query.setCriteria(new Month(2008, 1), FragmentField.CREATION_DATETIME);
			
		assertSortingWorks(query);
	}
	
	@Test
	public void fragmentsByFilter() throws Exception {
		FragmentsByFilter query = (FragmentsByFilter)this.object.getQuery(FragmentsByFilter.class);
		
		RawFilter filter = new RawFilter();
		filter.getIncludes().addTag(storedTag("tag"));
		query.setFilter(filter);
		
		assertSortingWorks(query);
	}
	
	@Test
	public void fragmentsByFilterWithPaging() throws Exception {
		FragmentsByFilter query = (FragmentsByFilter)this.object.getQuery(FragmentsByFilter.class);
		
		RawFilter filter = new RawFilter();
		filter.getIncludes().addTag(storedTag("tag"));
		query.setFilter(filter);
		
		query.setSortOption(new FragmentsSortOption(FragmentField.TITLE, true));
		
		assertEquals("a", query.getPage(1, 0).get(0).getTitle());	
		assertEquals("b", query.getPage(1, 1).get(0).getTitle());
		assertEquals("c", query.getPage(1, 2).get(0).getTitle());
	}
	
	@Test
	public void fragmentsByKeywords() throws Exception {
		if (this.object instanceof H2FragmentRepository) {
			FragmentsByKeywords query = (FragmentsByKeywords)this.object.getQuery(FragmentsByKeywords.class);
			query.setKeywords("hello");
			
			assertSortingWorks(query);
		}
	}
	
	@Test
	public void fragmentsByUser() throws Exception {
		FragmentsByUser query = (FragmentsByUser)this.object.getQuery(FragmentsByUser.class);
		query.setUserName(getPlainUser().getName());
		
		assertSortingWorks(query);
	}
	
	@Test
	public void fragmentsByIds() throws Exception {
		FragmentsByIds query = (FragmentsByIds)this.object.getQuery(FragmentsByIds.class);
		query.setIds(set(this.id1, this.id2, this.id3));
		
		assertSortingWorks(query);
	}
	
	@Test
	public void nullsLastIfAscending() throws Exception {
		this.object.register(newFragment());
		
		Page<Fragment> fragments = queryAll(this.orderByTitleAsc);
		
		assertEquals("a", fragments.get(0).getTitle());
		assertEquals("b", fragments.get(1).getTitle());
		assertEquals("c", fragments.get(2).getTitle());
		assertEquals(null, fragments.get(3).getTitle());
	}
	
	@Test
	public void nullsFirstIfDescending() throws Exception {
		this.object.register(newFragment());
		
		this.orderByTitleAsc.sortOption.ascending = false;
		Page<Fragment> fragments = queryAll(this.orderByTitleAsc);
		
		assertEquals(null, fragments.get(0).getTitle());	
		assertEquals("c", fragments.get(1).getTitle());
		assertEquals("b", fragments.get(2).getTitle());
		assertEquals("a", fragments.get(3).getTitle());
	}
	
	@Test
	public void ignoreCaseOfTitle() throws Exception {
		this.object.register(newFragmentWithTitle("B2"));
		
		Page<Fragment> fragments = queryAll(this.orderByTitleAsc);
		
		assertEquals("a", fragments.get(0).getTitle());
		assertEquals("b", fragments.get(1).getTitle());
		assertEquals("B2", fragments.get(2).getTitle());
		assertEquals("c", fragments.get(3).getTitle());
	}
}

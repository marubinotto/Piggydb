package marubinotto.piggydb.model.fragments;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import marubinotto.piggydb.model.Fragment;
import marubinotto.piggydb.model.FragmentRepository;
import marubinotto.piggydb.model.TagRepository;
import marubinotto.piggydb.model.entity.RawFilter;
import marubinotto.piggydb.model.enums.FragmentField;
import marubinotto.piggydb.model.query.FragmentsByFilter;
import marubinotto.piggydb.model.query.FragmentsSortOption;
import marubinotto.util.paging.Page;

import org.junit.Test;

public class FragmentsByFilterTest extends FragmentRepositoryTestBase {

	public FragmentsByFilterTest(RepositoryFactory<FragmentRepository> factory) {
		super(factory);
	}
	
	private FragmentsByFilter getQuery() throws Exception {
		return (FragmentsByFilter)this.object.getQuery(FragmentsByFilter.class);
	}
	
	@Test
	public void empty() throws Exception {
		this.object.register(newFragmentWithTitle("Daisuke"));
		
		FragmentsByFilter query = getQuery();
		query.setFilter(new RawFilter());
		Page<Fragment> result = query.getPage(10, 0);
		
		assertEquals(1, result.size());
		assertEquals("Daisuke", result.get(0).getTitle());
	}
	
	@Test
	public void oneTag() throws Exception {
		this.object.register(newFragmentWithTitleAndTags("Daisuke", "male"));
		
		FragmentsByFilter query = getQuery();
		
		// found
		RawFilter filter = new RawFilter();
		filter.getClassification().addTag(storedTag("male"));
		query.setFilter(filter);
		Page<Fragment> result = query.getPage(10, 0);
		
		assertEquals(1, result.size());
		assertEquals("Daisuke", result.get(0).getTitle());
		
		// not found
		this.object.getTagRepository().register(newTag("female"));
		filter = new RawFilter();
		filter.getClassification().addTag(storedTag("female"));
		query.setFilter(filter);
		result = query.getPage(10, 0);
		
		assertEquals(0, result.size());
	}
	
	@Test
	public void twoTags() throws Exception {
		this.object.register(newFragmentWithTitleAndTags("Daisuke", "tokyo", "male"));
		this.object.register(newFragmentWithTitleAndTags("Akane", "tokyo", "female"));
		this.object.register(newFragmentWithTitleAndTags("Chieko", "chiba", "female"));
		
		FragmentsByFilter query = getQuery();
		query.setSortOption(new FragmentsSortOption(FragmentField.FRAGMENT_ID, true));
		
		// one tag
		RawFilter filter = new RawFilter();
		filter.getClassification().addTag(storedTag("tokyo"));
		query.setFilter(filter);
		Page<Fragment> result = query.getPage(10, 0);
		
		assertEquals(2, result.size());
		assertEquals("Daisuke", result.get(0).getTitle());
		assertEquals("Akane", result.get(1).getTitle());
		
		// two tags
		filter = new RawFilter();
		filter.getClassification().addTag(storedTag("tokyo"));
		filter.getClassification().addTag(storedTag("female"));
		query.setFilter(filter);
		result = query.getPage(10, 0);
		
		assertEquals(1, result.size());
		assertEquals("Akane", result.get(0).getTitle());
	}
	
	@Test
	public void oneExcludeTag() throws Exception {
		this.object.register(newFragmentWithTitleAndTags("Daisuke", "male"));
		this.object.register(newFragmentWithTitleAndTags("Akane", "female"));
		
		FragmentsByFilter query = getQuery();
		
		RawFilter filter = new RawFilter();
		filter.getExcludes().addTag(storedTag("male"));
		query.setFilter(filter);
		Page<Fragment> result = query.getPage(10, 0);
		
		assertEquals(1, result.size());
		assertEquals("Akane", result.get(0).getTitle());
	}
	
	@Test
	public void oneTagAndOneExcludeTag() throws Exception {
		this.object.register(newFragmentWithTitleAndTags("Daisuke", "tokyo", "male"));
		this.object.register(newFragmentWithTitleAndTags("Akane", "tokyo", "female"));
		this.object.register(newFragmentWithTitleAndTags("Chieko", "chiba", "female"));
		
		FragmentsByFilter query = getQuery();
		
		RawFilter filter = new RawFilter();
		filter.getClassification().addTag(storedTag("tokyo"));
		filter.getExcludes().addTag(storedTag("male"));
		query.setFilter(filter);
		Page<Fragment> result = query.getPage(10, 0);
		
		assertEquals(1, result.size());
		assertEquals("Akane", result.get(0).getTitle());
	}
	
	@Test
	public void subordinateFragments() throws Exception {
		this.object.getTagRepository().register(newTagWithTags("cat", "animal"));
		this.object.register(newFragmentWithTitleAndTags("Animal Planet", "animal"));
		this.object.register(newFragmentWithTitleAndTags("Puss in Boots", "cat"));
		this.object.register(newFragmentWithTitleAndTags("Cherry Blossom", "plant"));
		
		FragmentsByFilter query = getQuery();
		query.setSortOption(new FragmentsSortOption(FragmentField.FRAGMENT_ID, true));
		
		RawFilter filter = new RawFilter();
		filter.getClassification().addTag(storedTag("animal"));
		query.setFilter(filter);
		Page<Fragment> result = query.getPage(10, 0);
		
		assertEquals(2, result.size());
		assertEquals("Animal Planet", result.get(0).getTitle());
		assertEquals("Puss in Boots", result.get(1).getTitle());
	}
	
	@Test
	public void paging() throws Exception {
		this.object.register(newFragmentWithTitle("Daisuke"));
		this.object.register(newFragmentWithTitle("Akane"));
		this.object.register(newFragmentWithTitle("Chieko"));
		
		FragmentsByFilter query = getQuery();
		query.setSortOption(new FragmentsSortOption(FragmentField.FRAGMENT_ID, true));
		
		RawFilter filter = new RawFilter();
		query.setFilter(filter);
		
		// page1
		Page<Fragment> page1 = query.getPage(1, 0);
		assertEquals(1, page1.size());
		assertEquals(3, page1.getTotalSize());
		assertTrue(page1.isFirstPage());
		assertFalse(page1.isLastPage());
		assertEquals("Daisuke", page1.get(0).getTitle());
		
		// page2
		Page<Fragment> page2 = query.getPage(1, 1);
		assertEquals(1, page2.size());
		assertEquals(3, page2.getTotalSize());
		assertFalse(page2.isFirstPage());
		assertFalse(page2.isLastPage());
		assertEquals("Akane", page2.get(0).getTitle());
		
		// page3
		Page<Fragment> page3 = query.getPage(1, 3);
		assertEquals(1, page3.size());
		assertEquals(3, page3.getTotalSize());
		assertFalse(page3.isFirstPage());
		assertTrue(page3.isLastPage());
		assertEquals("Chieko", page3.get(0).getTitle());
	}
	
	@Test
	public void foundByMultipleTags() throws Exception {
		TagRepository tagRepository = this.object.getTagRepository();
		tagRepository.register(newTagWithTags("bb", "aa"));
		tagRepository.register(newTagWithTags("cc", "aa"));		
		this.object.register(newFragmentWithTitleAndTags("title", "bb", "cc"));
		
		FragmentsByFilter query = getQuery();
		
		RawFilter filter = new RawFilter();
		filter.getClassification().addTag(storedTag("aa"));
		query.setFilter(filter);
		Page<Fragment> result = query.getPage(10, 0);
		
		assertEquals(1, result.getTotalSize());
		assertEquals(1, result.size());
		assertEquals("title", result.get(0).getTitle());
	}
	
	@Test
	// http://piggydb.lighthouseapp.com/projects/61149-piggydb/tickets/7
	public void orderByTitleWithCombinedTags() throws Exception {
		this.object.register(newFragmentWithTitleAndTags("Daisuke", "tokyo", "male"));
		this.object.register(newFragmentWithTitleAndTags("Anne", "tokyo", "female"));
		this.object.register(newFragmentWithTitleAndTags("Akane", "tokyo", "female"));
		
		FragmentsByFilter query = getQuery();
		query.setSortOption(new FragmentsSortOption(FragmentField.TITLE, true));
	
		RawFilter filter = new RawFilter();
		filter.getClassification().addTag(storedTag("tokyo"));
		filter.getExcludes().addTag(storedTag("male"));
		query.setFilter(filter);
		Page<Fragment> result = query.getPage(10, 0);
		
		assertEquals(2, result.size());
		assertEquals("Akane", result.get(0).getTitle());
		assertEquals("Anne", result.get(1).getTitle());
	}
}

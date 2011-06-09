package marubinotto.piggydb.model.fragments;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import marubinotto.piggydb.model.Fragment;
import marubinotto.piggydb.model.FragmentRepository;
import marubinotto.piggydb.model.FragmentsOptions;
import marubinotto.piggydb.model.TagRepository;
import marubinotto.piggydb.model.entity.RawFilter;
import marubinotto.piggydb.model.enums.FragmentField;
import marubinotto.util.paging.Page;

public class FindByFilterTest extends FragmentRepositoryTestBase {

	public FindByFilterTest(RepositoryFactory<FragmentRepository> factory) {
		super(factory);
	}
	
	private static final FragmentsOptions OPTIONS = new FragmentsOptions(10, 0, false);
	
	@Test
	public void empty() throws Exception {
		this.object.register(newFragmentWithTitle("Daisuke"));
		Page<Fragment> result = this.object.findByFilter(new RawFilter(), OPTIONS);
		assertEquals(1, result.size());
		assertEquals("Daisuke", result.get(0).getTitle());
	}
	
	@Test
	public void oneTag() throws Exception {
		this.object.register(newFragmentWithTitleAndTags("Daisuke", "male"));
		
		// found
		RawFilter filter = new RawFilter();
		filter.getClassification().addTag(storedTag("male"));
		Page<Fragment> result = this.object.findByFilter(filter, OPTIONS);
		assertEquals(1, result.size());
		assertEquals("Daisuke", result.get(0).getTitle());
		
		// not found
		this.object.getTagRepository().register(newTag("female"));
		filter = new RawFilter();
		filter.getClassification().addTag(storedTag("female"));
		result = this.object.findByFilter(filter, OPTIONS);
		assertEquals(0, result.size());
	}
	
	@Test
	public void twoTags() throws Exception {
		this.object.register(newFragmentWithTitleAndTags("Daisuke", "tokyo", "male"));
		this.object.register(newFragmentWithTitleAndTags("Akane", "tokyo", "female"));
		this.object.register(newFragmentWithTitleAndTags("Chieko", "chiba", "female"));
		OPTIONS.setSortOption(FragmentField.FRAGMENT_ID, true);
		
		// one tag
		RawFilter filter = new RawFilter();
		filter.getClassification().addTag(storedTag("tokyo"));
		Page<Fragment> result = this.object.findByFilter(filter, OPTIONS);
		assertEquals(2, result.size());
		assertEquals("Daisuke", result.get(0).getTitle());
		assertEquals("Akane", result.get(1).getTitle());
		
		// two tags
		filter = new RawFilter();
		filter.getClassification().addTag(storedTag("tokyo"));
		filter.getClassification().addTag(storedTag("female"));
		result = this.object.findByFilter(filter, OPTIONS);
		assertEquals(1, result.size());
		assertEquals("Akane", result.get(0).getTitle());
	}
	
	@Test
	public void oneExcludeTag() throws Exception {
		this.object.register(newFragmentWithTitleAndTags("Daisuke", "male"));
		this.object.register(newFragmentWithTitleAndTags("Akane", "female"));
		
		RawFilter filter = new RawFilter();
		filter.getExcludes().addTag(storedTag("male"));
		Page<Fragment> result = this.object.findByFilter(filter, OPTIONS);
		assertEquals(1, result.size());
		assertEquals("Akane", result.get(0).getTitle());
	}
	
	@Test
	public void oneTagAndOneExcludeTag() throws Exception {
		this.object.register(newFragmentWithTitleAndTags("Daisuke", "tokyo", "male"));
		this.object.register(newFragmentWithTitleAndTags("Akane", "tokyo", "female"));
		this.object.register(newFragmentWithTitleAndTags("Chieko", "chiba", "female"));
		
		RawFilter filter = new RawFilter();
		filter.getClassification().addTag(storedTag("tokyo"));
		filter.getExcludes().addTag(storedTag("male"));
		Page<Fragment> result = this.object.findByFilter(filter, OPTIONS);
		assertEquals(1, result.size());
		assertEquals("Akane", result.get(0).getTitle());
	}
	
	@Test
	public void subordinateFragments() throws Exception {
		this.object.getTagRepository().register(newTagWithTags("cat", "animal"));
		this.object.register(newFragmentWithTitleAndTags("Animal Planet", "animal"));
		this.object.register(newFragmentWithTitleAndTags("Puss in Boots", "cat"));
		this.object.register(newFragmentWithTitleAndTags("Cherry Blossom", "plant"));
		OPTIONS.setSortOption(FragmentField.FRAGMENT_ID, true);
		
		RawFilter filter = new RawFilter();
		filter.getClassification().addTag(storedTag("animal"));
		Page<Fragment> result = this.object.findByFilter(filter, OPTIONS);
		assertEquals(2, result.size());
		assertEquals("Animal Planet", result.get(0).getTitle());
		assertEquals("Puss in Boots", result.get(1).getTitle());
	}
	
	@Test
	public void paging() throws Exception {
		this.object.register(newFragmentWithTitle("Daisuke"));
		this.object.register(newFragmentWithTitle("Akane"));
		this.object.register(newFragmentWithTitle("Chieko"));
		
		RawFilter filter = new RawFilter();
		FragmentsOptions options = new FragmentsOptions();
		options.setSortOption(FragmentField.FRAGMENT_ID, true);
		
		// page1
		options.setPagingOption(1, 0);
		Page<Fragment> page1 = this.object.findByFilter(filter, options);
		assertEquals(1, page1.size());
		assertEquals(3, page1.getTotalSize());
		assertTrue(page1.isFirstPage());
		assertFalse(page1.isLastPage());
		assertEquals("Daisuke", page1.get(0).getTitle());
		
		// page2
		options.setPagingOption(1, 1);
		Page<Fragment> page2 = this.object.findByFilter(filter, options);
		assertEquals(1, page2.size());
		assertEquals(3, page2.getTotalSize());
		assertFalse(page2.isFirstPage());
		assertFalse(page2.isLastPage());
		assertEquals("Akane", page2.get(0).getTitle());
		
		// page3
		options.setPagingOption(1, 2);
		Page<Fragment> page3 = this.object.findByFilter(filter, options);
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
		
		RawFilter filter = new RawFilter();
		filter.getClassification().addTag(storedTag("aa"));
		Page<Fragment> result = this.object.findByFilter(filter, OPTIONS);
		
		assertEquals(1, result.getTotalSize());
		assertEquals(1, result.size());
		assertEquals("title", result.get(0).getTitle());
	}
	
	@Test
	// http://piggydb.lighthouseapp.com/projects/61149-piggydb/tickets/6
	public void orderByTitleWithCombinedTags() throws Exception {
		this.object.register(newFragmentWithTitleAndTags("Daisuke", "tokyo", "male"));
		this.object.register(newFragmentWithTitleAndTags("Akane", "tokyo", "female"));
		this.object.register(newFragmentWithTitleAndTags("Chieko", "chiba", "female"));
		
		RawFilter filter = new RawFilter();
		filter.getClassification().addTag(storedTag("tokyo"));
		filter.getExcludes().addTag(storedTag("male"));
		OPTIONS.setSortOption(FragmentField.TITLE, true);
		Page<Fragment> result = this.object.findByFilter(filter, OPTIONS);
		assertEquals(1, result.size());
		assertEquals("Akane", result.get(0).getTitle());
	}
}

package marubinotto.piggydb.model.fragments.filter;

import static marubinotto.util.time.DateTime.setCurrentTimeForTest;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import marubinotto.piggydb.model.Fragment;
import marubinotto.piggydb.model.FragmentRepository;
import marubinotto.piggydb.model.FragmentsOptions;
import marubinotto.piggydb.model.TagRepository;
import marubinotto.piggydb.model.entity.RawFilter;
import marubinotto.piggydb.model.fragments.FragmentRepositoryTestBase;
import marubinotto.util.paging.Page;

import org.junit.Before;
import org.junit.Test;

public class FindByFilterTest extends FragmentRepositoryTestBase {
	
	protected long id1;
	protected long id2;
	protected long id3;
	
	public FindByFilterTest(RepositoryFactory<FragmentRepository> factory) {
		super(factory);
	}

	@Before
	public void given() throws Exception {
		super.given();
		
		// Tags
		// - pingpong
		// - life
		//   - todo
		// - software
		//   - agile
		// - podcasting
		
		TagRepository tagRepository = this.object.getTagRepository();
		tagRepository.register(newTagWithTags("agile", "software"));
		tagRepository.register(newTag("podcasting"));

		setCurrentTimeForTest(2008, 1, 1);
		this.object.register(
			newFragmentWithTitleAndTags("Olympic Games, Beijing", "pingpong"));

		setCurrentTimeForTest(2008, 1, 2);
		this.object.register(
			newFragmentWithTitleAndTags("Restaurants", "life"));
		
		setCurrentTimeForTest(2008, 1, 3);
		this.object.register(
			newFragmentWithTitleAndTags("Pay the tax", "life", "todo"));

		setCurrentTimeForTest(2008, 1, 4);
		this.id1 = this.object.register(
			newFragmentWithTitleAndTags("Computer Software", "software"));

		setCurrentTimeForTest(2008, 1, 5);
		this.id2 = this.object.register(
			newFragmentWithTitleAndTags("Read the article 'The New Methodology'", "agile", "todo"));
		
		setCurrentTimeForTest(2008, 1, 6);
		this.id3 = this.object.register(
			newFragmentWithTitleAndTags("Adaptive Design", "agile"));

		setCurrentTimeForTest(null);
	}
	
	@Test
	public void withEmptyFilter() throws Exception {
		// When
		Page<Fragment> page = 
			this.object.findByFilter(new RawFilter(), new FragmentsOptions(10, 0, false));
		
		// Then
		assertEquals(6, page.size());		
		assertEquals("Adaptive Design", page.get(0).getTitle());	
		assertEquals("Read the article 'The New Methodology'", page.get(1).getTitle());		
		assertEquals("Computer Software", page.get(2).getTitle());	
		assertEquals("Pay the tax", page.get(3).getTitle());	
		assertEquals("Restaurants", page.get(4).getTitle());
		assertEquals("Olympic Games, Beijing", page.get(5).getTitle());
	}
	
	@Test
	public void withOneClassificationTag() throws Exception {
		// When
		RawFilter filter = new RawFilter();
		filter.getClassification().addTag(storedTag("pingpong"));
		Page<Fragment> page = 
			this.object.findByFilter(filter, new FragmentsOptions(10, 0, false));
		
		// Then
		assertEquals(1, page.size());		
		assertEquals("Olympic Games, Beijing", page.get(0).getTitle());
	}
	
	@Test
	public void withTwoClassificationTags() throws Exception {
		// When
		RawFilter filter = new RawFilter();
		filter.getClassification().addTag(storedTag("software"));
		filter.getClassification().addTag(storedTag("todo"));
		Page<Fragment> page = 
			this.object.findByFilter(filter, new FragmentsOptions(10, 0, false));
		
		// Then
		assertEquals(1, page.size());
		assertEquals("Read the article 'The New Methodology'", page.get(0).getTitle());
	}

	@Test
	public void withOneExcludingTag() throws Exception {
		// When
		RawFilter filter = new RawFilter();
		filter.getExcludes().addTag(storedTag("life"));
		Page<Fragment> page = 
			this.object.findByFilter(filter, new FragmentsOptions(10, 0, false));
		
		// Then
		assertEquals(4, page.size());
		assertEquals("Adaptive Design", page.get(0).getTitle());	
		assertEquals("Read the article 'The New Methodology'", page.get(1).getTitle());
		assertEquals("Computer Software", page.get(2).getTitle());
		assertEquals("Olympic Games, Beijing", page.get(3).getTitle());
	}

	@Test
	public void whenBothClassificationAndExcludes() throws Exception {
		// When
		RawFilter filter = new RawFilter();
		filter.getClassification().addTag(storedTag("life"));
		filter.getExcludes().addTag(storedTag("todo"));
		Page<Fragment> page = 
			this.object.findByFilter(filter, new FragmentsOptions(10, 0, false));
		
		// Then
		assertEquals(1, page.size());
		assertEquals("Restaurants", page.get(0).getTitle());
	}
	
	@Test
	public void shouldReturnAllSubordinateFragments() throws Exception {
		// When
		RawFilter filter = new RawFilter();
		filter.getClassification().addTag(storedTag("software"));
		Page<Fragment> page = 
			this.object.findByFilter(filter, new FragmentsOptions(10, 0, false));
		
		// Then
		assertEquals(3, page.size());
		assertEquals("Adaptive Design", page.get(0).getTitle());	
		assertEquals("Read the article 'The New Methodology'", page.get(1).getTitle());	
		assertEquals("Computer Software", page.get(2).getTitle());
	}
	
	@Test
	public void noFragmentsFound() throws Exception {
		// When
		RawFilter filter = new RawFilter();
		filter.getClassification().addTag(storedTag("podcasting"));
		Page<Fragment> page = 
			this.object.findByFilter(filter, new FragmentsOptions(10, 0, false));
		
		// Then
		assertEquals(0, page.size());
	}

	@Test
	public void paging() throws Exception {
		// When
		RawFilter filter = new RawFilter();
		filter.getClassification().addTag(storedTag("software"));
		
		Page<Fragment> page1 = this.object.findByFilter(filter, new FragmentsOptions(1, 0, false));
		Page<Fragment> page2 = this.object.findByFilter(filter, new FragmentsOptions(1, 1, false));
		Page<Fragment> page3 = this.object.findByFilter(filter, new FragmentsOptions(1, 2, false));
		
		// Then
		assertEquals(1, page1.size());
		assertEquals(3, page1.getTotalSize());
		assertTrue(page1.isFirstPage());
		
		assertEquals(1, page2.size());
		assertEquals(3, page2.getTotalSize());
		assertFalse(page2.isFirstPage());
		assertFalse(page2.isLastPage());
		
		assertEquals(1, page3.size());
		assertEquals(3, page3.getTotalSize());
		assertTrue(page3.isLastPage());
	}
}

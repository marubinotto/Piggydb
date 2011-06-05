package marubinotto.piggydb.model.fragments.filter;

import static org.junit.Assert.assertEquals;
import marubinotto.piggydb.model.Fragment;
import marubinotto.piggydb.model.FragmentRepository;
import marubinotto.piggydb.model.FragmentsOptions;
import marubinotto.piggydb.model.TagRepository;
import marubinotto.piggydb.model.entity.RawFilter;
import marubinotto.piggydb.model.fragments.FragmentRepositoryTestBase;
import marubinotto.util.paging.Page;

import org.junit.Before;
import org.junit.Test;

public class DuplicateFragmentsTest extends FragmentRepositoryTestBase {

	public DuplicateFragmentsTest(RepositoryFactory<FragmentRepository> factory) {
		super(factory);
	}
	
	@Before
	public void given() throws Exception {
		super.given();
		
		TagRepository tagRepository = this.object.getTagRepository();
		tagRepository.register(newTagWithTags("bb", "aa"));
		tagRepository.register(newTagWithTags("cc", "aa"));
		
		this.object.register(newFragmentWithTitleAndTags("title", "bb", "cc"));
	}
	
	@Test
	public void withOneClassificationTag() throws Exception {
		// When
		RawFilter filter = new RawFilter();
		filter.getClassification().addTag(storedTag("aa"));
		Page<Fragment> page = 
			this.object.findByFilter(filter, new FragmentsOptions(10, 0, false));
		
		// Then
		assertEquals(1, page.getTotalSize());
		assertEquals(1, page.size());
		assertEquals("title", page.get(0).getTitle());
	}
}

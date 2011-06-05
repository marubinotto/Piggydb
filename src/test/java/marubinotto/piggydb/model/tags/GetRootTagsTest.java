package marubinotto.piggydb.model.tags;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import marubinotto.piggydb.model.Tag;
import marubinotto.piggydb.model.TagRepository;
import marubinotto.util.paging.Page;

import org.junit.Before;
import org.junit.Test;

public class GetRootTagsTest extends TagRepositoryTestBase {
	
	public GetRootTagsTest(RepositoryFactory<TagRepository> factory) {
		super(factory);
	}
	
	@Before
	public void given() throws Exception {
		super.given();
		
		this.object.register(newTag("design"));
		this.object.register(newTagWithTags("OO", "design"));

		this.object.register(newTag("lang"));
		this.object.register(newTagWithTags("java", "lang"));
		this.object.register(newTagWithTags("ruby", "lang"));
	}

	@Test
	public void getRootTags() throws Exception {
		Page<Tag> results = this.object.getRootTags(3, 0);
		
		assertEquals(2, results.size());
		assertEquals(2, results.getTotalSize());
		assertEquals("design", results.get(0).getName());
		assertEquals("lang", results.get(1).getName());
	}
	
	@Test
	public void paging() throws Exception {
		// When
		Page<Tag> page1 = this.object.getRootTags(1, 0);
		Page<Tag> page2 = this.object.getRootTags(1, 1);
		
		// Then
		assertEquals(1, page1.size());
		assertEquals(2, page1.getTotalSize());
		assertTrue(page1.isFirstPage());
		
		assertEquals(1, page2.size());
		assertEquals(2, page2.getTotalSize());
		assertTrue(page2.isLastPage());
	}
}

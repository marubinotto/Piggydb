package marubinotto.piggydb.model.tags;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import marubinotto.piggydb.model.Tag;
import marubinotto.piggydb.model.TagRepository;
import marubinotto.util.paging.Page;

import org.junit.Before;
import org.junit.Test;

public class FindByParentTagTest extends TagRepositoryTestBase {
	
	private long tagId_lang;
	
	public FindByParentTagTest(RepositoryFactory<TagRepository> factory) {
		super(factory);
	}

	@Before
	public void given() throws Exception {
		super.given();
		
		this.tagId_lang = this.object.register(newTag("lang"));
		this.object.register(newTagWithTags("java", "lang"));
		this.object.register(newTagWithTags("ruby", "lang"));
		
		this.object.register(newTag("sports"));
		this.object.register(newTagWithTags("pingpong", "sports"));
		this.object.register(newTagWithTags("soccer", "sports"));
	}
	
	@Test
	public void findByParentTag() throws Exception {
		// When
		Page<Tag> page = this.object.findByParentTag(this.tagId_lang, 3, 0);
		
		// Then
		assertEquals(2, page.size());
		assertEquals("java", page.get(0).getName());
		assertEquals("ruby", page.get(1).getName());
	}
	
	@Test
	public void paging() throws Exception {
		// When
		Page<Tag> page1 = this.object.findByParentTag(this.tagId_lang, 1, 0);
		Page<Tag> page2 = this.object.findByParentTag(this.tagId_lang, 1, 1);
		
		// Then
		assertEquals(1, page1.size());
		assertEquals(2, page1.getTotalSize());
		assertTrue(page1.isFirstPage());
		
		assertEquals(1, page2.size());
		assertEquals(2, page2.getTotalSize());
		assertTrue(page2.isLastPage());
	}
}

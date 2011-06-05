package marubinotto.piggydb.model.tags;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertTrue;

import java.util.List;

import marubinotto.piggydb.model.Tag;
import marubinotto.piggydb.model.TagRepository;

import org.junit.Test;

public class GetPopularTagsTest extends TagRepositoryTestBase {

	public GetPopularTagsTest(RepositoryFactory<TagRepository> factory) {
		super(factory);
	}

	@Test
	public void empty() throws Exception {
		List<Tag> results = this.object.getPopularTags(10);
		assertTrue(results.isEmpty());
	}

	@Test
	public void oneNotUsed() throws Exception {
		this.object.register(newTag("foo"));
		
		List<Tag> results = this.object.getPopularTags(10);
		
		assertTrue(results.isEmpty());
	}
	
	@Test
	public void oneUsed() throws Exception {
		this.object.register(newTag("lang"));
		this.object.register(newTagWithTags("java", "lang"));
		
		List<Tag> results = this.object.getPopularTags(10);
		
		assertEquals(1, results.size());
		assertEquals("lang", results.get(0).getName());
		assertEquals(1, results.get(0).getPopularity().longValue());
	}
	
	@Test
	public void twoUsed() throws Exception {
		// Given
		this.object.register(newTag("lang"));
		this.object.register(newTagWithTags("java", "lang"));
		this.object.register(newTagWithTags("ruby", "lang"));
		this.object.register(newTagWithTags("javabeans", "java"));
		
		// When
		List<Tag> results = this.object.getPopularTags(10);
		
		// Then
		assertEquals(2, results.size());
		
		assertEquals("lang", results.get(0).getName());
		assertEquals(2, results.get(0).getPopularity().longValue());
		
		assertEquals("java", results.get(1).getName());
		assertEquals(1, results.get(1).getPopularity().longValue());
	}
	
	@Test
	public void oneOutOfTwo() throws Exception {
		// Given
		this.object.register(newTag("lang"));
		this.object.register(newTagWithTags("java", "lang"));
		this.object.register(newTagWithTags("ruby", "lang"));
		this.object.register(newTagWithTags("javabeans", "java"));
		
		// When
		List<Tag> results = this.object.getPopularTags(1);
		
		// Then
		assertEquals(1, results.size());
		
		assertEquals("lang", results.get(0).getName());
		assertEquals(2, results.get(0).getPopularity().longValue());
	}
}

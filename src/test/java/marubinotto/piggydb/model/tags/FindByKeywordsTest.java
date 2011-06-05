package marubinotto.piggydb.model.tags;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertTrue;
import marubinotto.piggydb.model.Tag;
import marubinotto.piggydb.model.TagRepository;
import marubinotto.util.paging.Page;

import org.junit.Before;
import org.junit.Test;

public class FindByKeywordsTest extends TagRepositoryTestBase {
	
	public FindByKeywordsTest(
			RepositoryFactory<TagRepository> factory) {
		super(factory);
	}

	@Before
	public void given() throws Exception {
		super.given();
		
		this.object.register(newTag("scala"));
		this.object.register(newTag("java"));
		this.object.register(newTag("java coffee"));
	}
	
	@Test
	public void nullKeyword() throws Exception {
		Page<Tag> result = this.object.findByKeywords(null, 5, 0);
		assertTrue(result.isEmpty());
	}
	
	@Test
	public void zeroHit() throws Exception {
		Page<Tag> result = this.object.findByKeywords("hoge", 5, 0);
		assertTrue(result.isEmpty());
	}
	
	@Test
	public void oneHit() throws Exception {
		Page<Tag> result = this.object.findByKeywords("scala", 5, 0);
		
		assertEquals(1, result.size());
		assertEquals("scala", result.get(0).getName());
	}
	
	@Test
	public void twoHit() throws Exception {
		Page<Tag> result = this.object.findByKeywords("java", 5, 0);
		
		assertEquals(2, result.size());
		assertEquals("java", result.get(0).getName());
		assertEquals("java coffee", result.get(1).getName());
	}
	
	@Test
	public void differentCase() throws Exception {
		Page<Tag> result = this.object.findByKeywords("SCALA", 5, 0);
		
		assertEquals(1, result.size());
		assertEquals("scala", result.get(0).getName());
	}
	
	@Test
	public void twoKeywords() throws Exception {
		Page<Tag> result = this.object.findByKeywords("coffee java", 5, 0);
		
		assertEquals(1, result.size());
		assertEquals("java coffee", result.get(0).getName());
	}
	
	@Test
	// http://sourceforge.net/apps/trac/piggydb/ticket/19
	public void onlyDelimiters() throws Exception {
		Page<Tag> result = this.object.findByKeywords("()", 5, 0);
		
		assertTrue(result.isEmpty());
	}
}

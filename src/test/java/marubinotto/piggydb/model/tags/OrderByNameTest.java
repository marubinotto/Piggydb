package marubinotto.piggydb.model.tags;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertTrue;
import marubinotto.piggydb.model.Tag;
import marubinotto.piggydb.model.TagRepository;
import marubinotto.util.paging.Page;

import org.junit.Test;

public class OrderByNameTest extends TagRepositoryTestBase {

	public OrderByNameTest(RepositoryFactory<TagRepository> factory) {
		super(factory);
	}

	@Test
	public void empty() throws Exception {
		Page<Tag> results = this.object.orderByName(5, 0);
		assertTrue(results.isEmpty());
	}
	
	@Test
	public void one() throws Exception {
		this.object.register(newTag("apple"));
		
		Page<Tag> results = this.object.orderByName(5, 0);
		
		assertEquals(1, results.size());
		assertEquals("apple", results.get(0).getName());
	}
	
	@Test
	public void two() throws Exception {
		this.object.register(newTag("orange"));
		this.object.register(newTag("apple"));
		
		Page<Tag> results = this.object.orderByName(5, 0);
		
		assertEquals(2, results.size());
		assertEquals("apple", results.get(0).getName());
		assertEquals("orange", results.get(1).getName());
	}
	
	@Test
	public void paging() throws Exception {
		this.object.register(newTag("orange"));
		this.object.register(newTag("apple"));
		
		Page<Tag> page1 = this.object.orderByName(1, 0);
		Page<Tag> page2 = this.object.orderByName(1, 1);
		
		assertEquals(1, page1.size());
		assertEquals("apple", page1.get(0).getName());
		
		assertEquals(1, page2.size());
		assertEquals("orange", page2.get(0).getName());
	}
	
	@Test
	public void ignoreCase() throws Exception {
		this.object.register(newTag("orange"));
		this.object.register(newTag("apple"));
		this.object.register(newTag("OS"));
		
		Page<Tag> results = this.object.orderByName(5, 0);
		
		assertEquals(3, results.size());
		assertEquals("apple", results.get(0).getName());
		assertEquals("orange", results.get(1).getName());
		assertEquals("OS", results.get(2).getName());
	}
}

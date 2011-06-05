package marubinotto.piggydb.model.tags;

import static marubinotto.util.CollectionUtils.set;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.HashSet;
import java.util.Map;

import marubinotto.piggydb.model.TagRepository;

import org.junit.Before;
import org.junit.Test;

public class GetNamesTest extends TagRepositoryTestBase {

	protected long tag1Id;
	protected long tag2Id;
	
	public GetNamesTest(RepositoryFactory<TagRepository> factory) {
		super(factory);
	}
	
	@Before
	public void given() throws Exception {
		super.given();
		
		this.tag1Id = this.object.register(newTag("tag1"));
		this.tag2Id = this.object.register(newTag("tag2"));
	}
	
	@Test
	public void getWithEmptySet() throws Exception {
		Map<Long, String> results = this.object.getNames(new HashSet<Long>());
		assertTrue(results.isEmpty());
	}
	
	@Test
	public void getWithTwoIds() throws Exception {
		Map<Long, String> results = this.object.getNames(set(this.tag1Id, this.tag2Id));
		
		assertEquals(2, results.size());
		assertEquals("tag1", results.get(this.tag1Id));
		assertEquals("tag2", results.get(this.tag2Id));
	}
	
	@Test
	public void getWithOneExistentAndOneNonexistentIds() throws Exception {
		Map<Long, String> results = this.object.getNames(set(this.tag1Id, 123L));
		
		assertEquals(1, results.size());
		assertEquals("tag1", results.get(this.tag1Id));
	}
}

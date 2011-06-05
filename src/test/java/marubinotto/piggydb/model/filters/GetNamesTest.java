package marubinotto.piggydb.model.filters;

import static marubinotto.util.CollectionUtils.set;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.HashSet;
import java.util.Map;

import marubinotto.piggydb.model.FilterRepository;

import org.junit.Before;
import org.junit.Test;

public class GetNamesTest extends FilterRepositoryTestBase {

	protected long filter1Id;
	protected long filter2Id;
	
	public GetNamesTest(RepositoryFactory<FilterRepository> factory) {
		super(factory);
	}
	
	@Before
	public void given() throws Exception {
		super.given();
		
		this.filter1Id = this.object.register(newFilter("filter1"));
		this.filter2Id = this.object.register(newFilter("filter2"));
	}
	
	@Test
	public void getNamesWithEmptySet() throws Exception {
		Map<Long, String> results = this.object.getNames(new HashSet<Long>());
		assertTrue(results.isEmpty());
	}
	
	
	@Test
	public void getNamesWithTwoExistentIds() throws Exception {
		Map<Long, String> results = this.object.getNames(set(this.filter1Id, this.filter2Id));
		
		assertEquals(2, results.size());
		assertEquals("filter1", results.get(this.filter1Id));
		assertEquals("filter2", results.get(this.filter2Id));
	}
	
	@Test
	public void getNamesWithOneExistentAndOneNonexistentIds() throws Exception {
		Map<Long, String> results = this.object.getNames(set(this.filter1Id, 123L));
		
		assertEquals(1, results.size());
		assertEquals("filter1", results.get(this.filter1Id));
	}
}

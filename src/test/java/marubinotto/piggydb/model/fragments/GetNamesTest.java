package marubinotto.piggydb.model.fragments;

import static marubinotto.util.CollectionUtils.set;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.HashSet;
import java.util.Map;

import marubinotto.piggydb.model.FragmentRepository;

import org.junit.Before;
import org.junit.Test;

public class GetNamesTest extends FragmentRepositoryTestBase {
	
	public GetNamesTest(RepositoryFactory<FragmentRepository> factory) {
		super(factory);
	}

	protected long id1;
	protected long id2;
	
	@Before
	public void given() throws Exception {
		super.given();
		
		this.id1 = this.object.register(newFragmentWithTitle("Fragment1"));
		this.id2 = this.object.register(newFragmentWithTitle("Fragment2"));
	}
	
	@Test
	public void getTitlesWithEmptySet() throws Exception {
		Map<Long, String> results = this.object.getNames(new HashSet<Long>());
		assertTrue(results.isEmpty());
	}
	
	@Test
	public void getTitlesWithTwoExistentIds() throws Exception {
		Map<Long, String> results = this.object.getNames(set(this.id1, this.id2));
		
		assertEquals(2, results.size());
		assertEquals("Fragment1", results.get(this.id1));
		assertEquals("Fragment2", results.get(this.id2));
	}
	
	@Test
	public void getTitlesWithOneExistentAndOneNonexistentIds() throws Exception {
		Map<Long, String> results = this.object.getNames(set(this.id1, 123L));
		
		assertEquals(1, results.size());
		assertEquals("Fragment1", results.get(this.id1));
	}
}

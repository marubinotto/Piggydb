package marubinotto.piggydb.model.fragments;

import static marubinotto.util.CollectionUtils.set;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.List;

import marubinotto.piggydb.model.Fragment;
import marubinotto.piggydb.model.FragmentRepository;
import marubinotto.piggydb.model.query.FragmentsByIds;

import org.junit.Before;
import org.junit.Test;

public class FragmentsByIdsTest extends FragmentRepositoryTestBase {

	protected long id1;
	protected long id2;
	
	public FragmentsByIdsTest(RepositoryFactory<FragmentRepository> factory) {
		super(factory);
	}

	@Before
	public void given() throws Exception {
		super.given();
		
		this.id1 = this.object.register(newFragmentWithTitle("title1"));
		this.id2 = this.object.register(newFragmentWithTitle("title2"));
	}
	
	private FragmentsByIds getQuery() throws Exception {
		return (FragmentsByIds)this.object.getQuery(FragmentsByIds.class);
	}
	
	@Test
	public void noSuchId() throws Exception {
		FragmentsByIds query = getQuery();
		query.setIds(set(123L));
		List<Fragment> fragments = query.getAll();
		
		assertTrue(fragments.isEmpty());
	}
	
	@Test
	public void oneExisting() throws Exception {
		FragmentsByIds query = getQuery();
		query.setIds(set(this.id1));
		List<Fragment> fragments = query.getAll();
		
		assertEquals(1, fragments.size());
		assertEquals("title1", fragments.get(0).getTitle());
	}
	
	@Test
	public void oneExistingAndOneNonexisting() throws Exception {
		FragmentsByIds query = getQuery();
		query.setIds(set(this.id1, 123L));
		List<Fragment> fragments = query.getAll();
		
		assertEquals(1, fragments.size());
		assertEquals("title1", fragments.get(0).getTitle());
	}
}

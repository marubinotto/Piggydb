package marubinotto.piggydb.model.fragments;

import static marubinotto.util.CollectionUtils.set;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.List;

import marubinotto.piggydb.model.Fragment;
import marubinotto.piggydb.model.FragmentRepository;
import marubinotto.piggydb.model.FragmentsOptions.SortOption;

import org.junit.Before;
import org.junit.Test;

public class GetByIdsTest extends FragmentRepositoryTestBase {

	protected long id1;
	protected long id2;
	
	public GetByIdsTest(RepositoryFactory<FragmentRepository> factory) {
		super(factory);
	}

	@Before
	public void given() throws Exception {
		super.given();
		
		this.id1 = this.object.register(newFragmentWithTitle("title1"));
		this.id2 = this.object.register(newFragmentWithTitle("title2"));
	}
	
	@Test
	public void noSuchId() throws Exception {
		List<Fragment> fragments = 
			this.object.getByIds(set(123L), SortOption.getDefault(), false);
		assertTrue(fragments.isEmpty());
	}
	
	@Test
	public void oneExisting() throws Exception {
		List<Fragment> fragments = 
			this.object.getByIds(set(this.id1), SortOption.getDefault(), false);
		
		assertEquals(1, fragments.size());
		assertEquals("title1", fragments.get(0).getTitle());
	}
	
	@Test
	public void oneExistingAndOneNonexisting() throws Exception {
		List<Fragment> fragments = 
			this.object.getByIds(set(this.id1, 123L), SortOption.getDefault(), false);
		
		assertEquals(1, fragments.size());
		assertEquals("title1", fragments.get(0).getTitle());
	}
}

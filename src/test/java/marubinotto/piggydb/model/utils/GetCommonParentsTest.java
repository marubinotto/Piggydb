package marubinotto.piggydb.model.utils;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;

import marubinotto.piggydb.model.Fragment;
import marubinotto.piggydb.model.ModelUtils;
import marubinotto.piggydb.model.entity.RawFragment;

import org.junit.Test;

public class GetCommonParentsTest {

	@Test
	public void zeroFragments() throws Exception {
		List<Fragment>parents = ModelUtils.getCommonParents(new ArrayList<Fragment>());
		assertTrue(parents.isEmpty());
	}
	
	@Test
	public void oneFragmentWithoutParents() throws Exception {
		List<Fragment> fragments = new ArrayList<Fragment>();
		fragments.add(new RawFragment());
		
		List<Fragment>parents = ModelUtils.getCommonParents(fragments);
		
		assertTrue(parents.isEmpty());
	}
	
	@Test
	public void oneFragmentWithOneParent() throws Exception {
		List<Fragment> fragments = new ArrayList<Fragment>();
		fragments.add(fragmentWithParents(1L));
		
		List<Fragment>parents = ModelUtils.getCommonParents(fragments);
		
		assertEquals(1, parents.size());
		assertEquals(1L, parents.get(0).getId().longValue());
	}
	
	@Test
	public void twoFragmentsWithoutCommonParents() throws Exception {
		List<Fragment> fragments = new ArrayList<Fragment>();
		fragments.add(fragmentWithParents(1L));
		fragments.add(fragmentWithParents(2L));
		
		List<Fragment>parents = ModelUtils.getCommonParents(fragments);
		
		assertTrue(parents.isEmpty());
	}
	
	@Test
	public void twoFragmentsWithOneCommonParents() throws Exception {
		List<Fragment> fragments = new ArrayList<Fragment>();
		fragments.add(fragmentWithParents(1L, 2L));
		fragments.add(fragmentWithParents(2L, 3L));
		
		List<Fragment>parents = ModelUtils.getCommonParents(fragments);
		
		assertEquals(1, parents.size());
		assertEquals(2L, parents.get(0).getId().longValue());
	}
	
	private static Fragment fragmentWithParents(long ... ids) {
		RawFragment fragment = new RawFragment();
		for (long id : ids) {
			RawFragment parent = new RawFragment();
			parent.setId(id);
			fragment.addParent(parent);
		}
		return fragment;
	}
}

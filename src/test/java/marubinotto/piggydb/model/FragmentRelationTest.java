package marubinotto.piggydb.model;

import static org.junit.Assert.*;
import marubinotto.piggydb.model.entity.RawFragment;

import org.junit.Test;

public class FragmentRelationTest {

	@Test
	public void isSamePairAs() throws Exception {
		FragmentRelation relation1 = new FragmentRelation(fragment(1), fragment(2)); 
		FragmentRelation relation2 = new FragmentRelation(fragment(1), fragment(2)); 
		FragmentRelation relation3 = new FragmentRelation(fragment(2), fragment(1)); 
		FragmentRelation relation4 = new FragmentRelation(fragment(2), fragment(3)); 
		
		assertTrue("Same pairs in the same order", relation1.isSamePairAs(relation2));
		assertTrue("Same pairs in reverse order", relation1.isSamePairAs(relation3));
		assertFalse("Different pairs", relation1.isSamePairAs(relation4));
	}
	
	static RawFragment fragment(long id) {
		RawFragment fragment = new RawFragment();
		fragment.setId(id);
		return fragment;
	}
}

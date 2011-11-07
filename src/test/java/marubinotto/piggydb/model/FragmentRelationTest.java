package marubinotto.piggydb.model;

import static marubinotto.piggydb.fixture.EntityFixtures.fragment;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

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
}

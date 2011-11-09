package marubinotto.piggydb.model;

import static marubinotto.piggydb.fixture.EntityFixtures.fragment;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class FragmentRelationTest {

	@Test
	public void isSamePairAs() throws Exception {
		FragmentRelation relation1 = new FragmentRelation(fragment(1L), fragment(2L)); 
		FragmentRelation relation2 = new FragmentRelation(fragment(1L), fragment(2L)); 
		FragmentRelation relation3 = new FragmentRelation(fragment(2L), fragment(1L)); 
		FragmentRelation relation4 = new FragmentRelation(fragment(2L), fragment(3L)); 
		
		assertTrue("Same pairs in the same order", relation1.isSamePairAs(relation2));
		assertTrue("Same pairs in reverse order", relation1.isSamePairAs(relation3));
		assertFalse("Different pairs", relation1.isSamePairAs(relation4));
	}
}

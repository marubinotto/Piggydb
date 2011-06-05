package marubinotto.piggydb.model.fragment.relation;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import marubinotto.piggydb.model.FragmentRelation;
import marubinotto.piggydb.model.entity.RawFragment;

import org.junit.Before;
import org.junit.Test;

public class GetParentRelationByParentIdTest {

	private RawFragment object = new RawFragment();
	
	@Before
	public void given() throws Exception {
		RawFragment parent1 = new RawFragment();
		parent1.setId(1L);
		this.object.addParent(parent1);
		
		RawFragment parent2 = new RawFragment();
		parent2.setId(2L);
		this.object.addParent(parent2);
	}
	
	@Test
	public void noSuchParent() throws Exception {
		FragmentRelation relation = this.object.getParentRelationByParentId(100L);
		assertNull(relation);
	}
	
	@Test
	public void getParentRelation() throws Exception {
		FragmentRelation relation = this.object.getParentRelationByParentId(1L);
		assertEquals(1L, relation.from.getId().longValue());
	}
}

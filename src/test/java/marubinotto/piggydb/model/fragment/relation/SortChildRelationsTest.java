package marubinotto.piggydb.model.fragment.relation;

import static marubinotto.util.CollectionUtils.list;
import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import marubinotto.piggydb.model.FragmentRelation;
import marubinotto.piggydb.model.entity.RawFragment;

public class SortChildRelationsTest {

	private RawFragment object = new RawFragment();
	
	private static FragmentRelation createChildRelation(Long id, Integer priority) {
		FragmentRelation relation = new FragmentRelation();
		relation.setId(id);
		relation.priority = priority;
		relation.to = new RawFragment(); 
		return relation;
	}
	
	private List<Long> getChildrenOrder() {
		List<Long> order = new ArrayList<Long>();
		for (FragmentRelation relation : this.object.getChildRelations()) {
			order.add(relation.getId());
		}
		return order;
	}
	
	@Test
	public void sortByPriority() throws Exception {
		this.object.addChildRelation(createChildRelation(1L, 1));
		this.object.addChildRelation(createChildRelation(2L, 2));
		
		this.object.sortChildRelations();
		
		assertEquals(list(2L, 1L), getChildrenOrder());
	}
	
	@Test
	public void sortByIdWhenPriorityIsSame() throws Exception {
		this.object.addChildRelation(createChildRelation(1L, 0));
		this.object.addChildRelation(createChildRelation(2L, 0));
		
		this.object.sortChildRelations();
		
		assertEquals(list(1L, 2L), getChildrenOrder());
	}
	
	@Test
	public void nullPriorityIsLow() throws Exception {
		this.object.addChildRelation(createChildRelation(1L, null));
		this.object.addChildRelation(createChildRelation(2L, 0));
		
		this.object.sortChildRelations();
		
		assertEquals(list(2L, 1L), getChildrenOrder());
	}
}

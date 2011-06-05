package marubinotto.piggydb.model.fragments.relation;

import static marubinotto.util.CollectionUtils.list;
import static org.junit.Assert.assertEquals;

import java.util.ArrayList;

import marubinotto.piggydb.model.Fragment;
import marubinotto.piggydb.model.FragmentRepository;
import marubinotto.piggydb.model.fragments.FragmentRepositoryTestBase;

import org.junit.Before;
import org.junit.Test;

public class ChildRelationPriorityTest extends FragmentRepositoryTestBase {
	
	private Long parentId;
	
	private Long relation1;
	private Long relation2;
	private Long relation3;

	public ChildRelationPriorityTest(RepositoryFactory<FragmentRepository> factory) {
		super(factory);
	}
	
	@Before
	public void given() throws Exception {
		super.given();
		
		this.parentId = this.object.register(newFragment());
		Long child1Id = this.object.register(newFragment());
		Long child2Id = this.object.register(newFragment());
		Long child3Id = this.object.register(newFragment());
		
		this.relation1 = this.object.createRelation(this.parentId, child1Id, getPlainUser());
		this.relation2 = this.object.createRelation(this.parentId, child2Id, getPlainUser());
		this.relation3 = this.object.createRelation(this.parentId, child3Id, getPlainUser());
	}
	
	@Test
	public void defaultPriorities() throws Exception {
		assertEquals(0, this.object.getRelation(this.relation1).priority.intValue());
		assertEquals(0, this.object.getRelation(this.relation2).priority.intValue());
		assertEquals(0, this.object.getRelation(this.relation3).priority.intValue());	
	}
	
	@Test
	public void updateChildRelationPriorities() throws Exception {
		Fragment parent = this.object.get(this.parentId);
		this.object.updateChildRelationPriorities(parent, list(this.relation1, this.relation3), getPlainUser());
		
		assertEquals(2, this.object.getRelation(this.relation1).priority.intValue());
		assertEquals(0, this.object.getRelation(this.relation2).priority.intValue());
		assertEquals(1, this.object.getRelation(this.relation3).priority.intValue());
	}
	
	@Test
	public void emptyOrder() throws Exception {
		Fragment parent = this.object.get(this.parentId);
		this.object.updateChildRelationPriorities(parent, new ArrayList<Long>(), getPlainUser());
		
		assertEquals(0, this.object.getRelation(this.relation1).priority.intValue());
		assertEquals(0, this.object.getRelation(this.relation2).priority.intValue());
		assertEquals(0, this.object.getRelation(this.relation3).priority.intValue());
	}
}

package marubinotto.piggydb.model.authorization;

import static junit.framework.Assert.fail;
import static marubinotto.util.CollectionUtils.list;
import static org.junit.Assert.assertEquals;
import marubinotto.piggydb.model.Fragment;
import marubinotto.piggydb.model.FragmentRepository;
import marubinotto.piggydb.model.exception.AuthorizationException;
import marubinotto.piggydb.model.fragments.FragmentRepositoryTestBase;

import org.junit.Before;
import org.junit.Test;

public class UpdateChildRelationPrioritiesTest extends FragmentRepositoryTestBase {

	private long parentId;
	private Fragment parent;
	
	private long relation1Id;
	private long relation2Id;

	public UpdateChildRelationPrioritiesTest(RepositoryFactory<FragmentRepository> factory) {
		super(factory);
	}
	
	@Before
	public void given() throws Exception {
		super.given();
		
		this.parentId = this.object.register(newFragment());
		long id1 = this.object.register(newFragment());
		long id2 = this.object.register(newFragment());
		
		this.relation1Id = this.object.createRelation(this.parentId, id1, getPlainUser());
		this.relation2Id = this.object.createRelation(this.parentId, id2, getPlainUser());
		
		this.parent = this.object.get(this.parentId);
	}
	
	@Test
	public void plainUserCan() throws Exception {
		this.object.updateChildRelationPriorities(
			this.parent, list(this.relation1Id, this.relation2Id), getPlainUser());
	}
	
	@Test
	public void viewerCannot() throws Exception {
		try {
			this.object.updateChildRelationPriorities(
				this.parent, list(this.relation1Id, this.relation2Id), getViewer());
			fail();
		} 
		catch (AuthorizationException e) {
			assertEquals(AuthErrors.toChangeFragment(this.parent), e);
		}
		
		// Ensure not updated
		assertEquals(0, this.object.getRelation(this.relation1Id).priority.intValue());
		assertEquals(0, this.object.getRelation(this.relation2Id).priority.intValue());
	}
}

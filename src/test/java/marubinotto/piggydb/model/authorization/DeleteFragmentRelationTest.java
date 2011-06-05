package marubinotto.piggydb.model.authorization;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.fail;
import marubinotto.piggydb.model.AuthorizationException;
import marubinotto.piggydb.model.FragmentRepository;
import marubinotto.piggydb.model.fragments.FragmentRepositoryTestBase;

import org.junit.Before;
import org.junit.Test;

public class DeleteFragmentRelationTest extends FragmentRepositoryTestBase {
	
	protected long relationId;

	public DeleteFragmentRelationTest(RepositoryFactory<FragmentRepository> factory) {
		super(factory);
	}
	
	@Before
	public void given() throws Exception {
		super.given();
		
		long id1 = this.object.register(newFragmentWithTitle("Piggydb"));
		long id2 = this.object.register(newFragmentWithTitle("What is Piggydb?"));
		this.relationId = this.object.createRelation(id1, id2, getPlainUser());
	}
	
	@Test
	public void plainUserCan() throws Exception {
		this.object.deleteRelation(this.relationId, getPlainUser());
	}
	
	@Test
	public void viewerCannot() throws Exception {
		try {
			this.object.deleteRelation(this.relationId, getViewer());
			fail();
		} 
		catch (AuthorizationException e) {
			assertEquals(AuthErrors.toDeleteFragmentRelation(), e);
		}
		assertNotNull(this.object.getRelation(this.relationId));
	}
}

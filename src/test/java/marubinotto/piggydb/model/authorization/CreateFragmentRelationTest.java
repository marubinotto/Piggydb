package marubinotto.piggydb.model.authorization;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.fail;
import marubinotto.piggydb.model.AuthorizationException;
import marubinotto.piggydb.model.FragmentRepository;
import marubinotto.piggydb.model.fragments.FragmentRepositoryTestBase;

import org.junit.Before;
import org.junit.Test;

public class CreateFragmentRelationTest extends FragmentRepositoryTestBase {
	
	protected long id1;
	protected long id2;

	public CreateFragmentRelationTest(RepositoryFactory<FragmentRepository> factory) {
		super(factory);
	}
	
	@Before
	public void given() throws Exception {
		super.given();
		this.id1 = this.object.register(newFragmentWithTitle("Piggydb"));
		this.id2 = this.object.register(newFragmentWithTitle("What is Piggydb?"));
	}
	
	@Test
	public void plainUserCan() throws Exception {
		Long newId = this.object.createRelation(this.id1, this.id2, getPlainUser());
		assertNotNull(newId);
	}
	
	@Test
	public void viewerCannot() throws Exception {
		try {
			this.object.createRelation(this.id1, this.id2, getViewer());
			fail();
		} 
		catch (AuthorizationException e) {
			assertEquals(AuthErrors.toCreateFragmentRelation(), e);
		}
	}
}

package marubinotto.piggydb.model.authorization;

import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertTrue;
import marubinotto.piggydb.model.FragmentRelation;

import org.junit.Before;
import org.junit.Test;

public class FragmentRelationTest extends AuthorizationTestBase {

	private FragmentRelation object = new FragmentRelation();
	
	@Before
	public void given() throws Exception {
		super.given();
	}
	
	// Can delete
	
	@Test
	public void plainUserCanDelete() throws Exception {
		assertTrue(this.object.canDelete(getPlainUser()));
	}
	
	@Test
	public void viewerCannotDelete() throws Exception {
		assertFalse(this.object.canDelete(getViewer()));
	}
}

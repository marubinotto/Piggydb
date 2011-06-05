package marubinotto.piggydb.model.authorization;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.fail;
import marubinotto.piggydb.model.AuthorizationException;
import marubinotto.piggydb.model.entity.RawFragment;

import org.junit.Before;
import org.junit.Test;

public class CreateFragmentTest extends AuthorizationTestBase {

	@Before
	public void given() throws Exception {
		super.given();
	}
	
	@Test
	public void plainUserCan() throws Exception {
		new RawFragment(getPlainUser());
	}	
	
	@Test
	public void viewerCannot() throws Exception {
		try {
			new RawFragment(getViewer());
			fail();
		} 
		catch (AuthorizationException e) {
			assertEquals(AuthErrors.toCreateFragment(), e);
		}
	}
}

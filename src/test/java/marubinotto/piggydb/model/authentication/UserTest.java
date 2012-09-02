package marubinotto.piggydb.model.authentication;

import static junit.framework.Assert.assertEquals;
import marubinotto.piggydb.model.auth.User;
import marubinotto.piggydb.model.enums.Role;

import org.junit.Test;

public class UserTest {

	private User object = new User();
	
	@Test
	public void noRolesByDefault() throws Exception {
		assertEquals(false, this.object.isOwner());
		assertEquals(false, this.object.isViewer());
		assertEquals(false, this.object.isInternalUser());
	}
	
	@Test
	public void addRole() throws Exception {
		assertEquals(false, this.object.isInRole(Role.DEFAULT));
		this.object.addRole(Role.DEFAULT);
		assertEquals(true, this.object.isInRole(Role.DEFAULT));
	}
	
	@Test
	public void addRoles() throws Exception {
		this.object.addRole(Role.DEFAULT);
		this.object.addRole(Role.INTERNAL_USER);
		
		assertEquals(true, this.object.isInRole(Role.DEFAULT));
		assertEquals(true, this.object.isInRole(Role.INTERNAL_USER));
	}
	
	@Test
	public void isInternalUser() throws Exception {
		this.object.addRole(Role.INTERNAL_USER);
		assertEquals(true, this.object.isInternalUser());
	}
}

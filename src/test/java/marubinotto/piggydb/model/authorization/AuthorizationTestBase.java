package marubinotto.piggydb.model.authorization;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.fail;
import marubinotto.piggydb.model.Tag;
import marubinotto.piggydb.model.User;
import marubinotto.piggydb.model.enums.Role;
import marubinotto.piggydb.model.exception.AuthorizationException;

import org.junit.Before;

public abstract class AuthorizationTestBase {

	private User owner = new User("whatever");
	private User plainUser = new User("plain");
	private User viewer = new User("viewer");
	
	@Before
	public void given() throws Exception {
		this.owner.addRole(Role.OWNER);
		this.viewer.addRole(Role.VIEWER);
	}

	public User getOwner() {
		return this.owner;
	}

	public User getPlainUser() {
		return this.plainUser;
	}

	public User getViewer() {
		return this.viewer;
	}
	
	protected static void cannotRename(Tag tag, String newName, User user, String notAuthTag) 
	throws Exception {
		String originalName = tag.getName();
		try {
			tag.setNameByUser(newName, user);
			fail();
		} 
		catch (AuthorizationException e) {
			assertEquals(AuthErrors.forTag(notAuthTag), e);
		}
		assertEquals(originalName, tag.getName());
	}
}

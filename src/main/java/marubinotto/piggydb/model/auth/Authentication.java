package marubinotto.piggydb.model.auth;

import marubinotto.piggydb.model.enums.Role;
import marubinotto.util.Assert;

public class Authentication {

	private OwnerAuth ownerAuth;
	private DefaultAuth defaultAuth;

	private boolean enableGuest = false;
	private static final String GUEST = "guest";

	private boolean enableAnonymous = false;
	private static final String ANONYMOUS = "anonymous";

	public void setOwnerAuth(OwnerAuth ownerAuth) {
		this.ownerAuth = ownerAuth;
	}

	public void setDefaultAuth(DefaultAuth defaultAuth) {
		this.defaultAuth = defaultAuth;
	}

	public void setEnableGuest(boolean enableGuest) {
		this.enableGuest = enableGuest;
	}

	public void setEnableAnonymous(boolean enableAnonymous) {
		this.enableAnonymous = enableAnonymous;
	}

	public boolean isEnableAnonymous() {
		return this.enableAnonymous;
	}

	public User authenticate(String userName, String password) throws Exception {
		Assert.Arg.notNull(userName, "userName");
		Assert.Arg.notNull(password, "password");
		Assert.Property.requireNotNull(ownerAuth, "ownerAuth");
		Assert.Property.requireNotNull(defaultAuth, "defaultAuth");

		User user = new User(userName);
		user.addRole(Role.DEFAULT);

		if (this.enableGuest && userName.equals(GUEST) && password.equals(GUEST)) {
			return user;
		}

		if (this.ownerAuth.authorizeAsOwner(user, password)) {
			return user;
		}

		if (this.defaultAuth.authorizeAsNormalUser(user, password)) {
			return user;
		}

		return null;
	}

	public User authenticateAsAnonymous() {
		if (!this.enableAnonymous) return null;

		User user = new User(ANONYMOUS);
		user.setAnonymous(true);
		user.addRole(Role.DEFAULT);
		user.addRole(Role.VIEWER);
		return user;
	}
}

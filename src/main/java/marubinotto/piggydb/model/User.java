package marubinotto.piggydb.model;

import marubinotto.piggydb.model.enums.Role;
import marubinotto.util.Assert;
import marubinotto.util.web.GenericUser;

public class User extends GenericUser {

	public Long homeFragmentId;

	private boolean sessionPersisted = false;
	private boolean anonymous = false;

	public User() {
	}

	public User(String name) {
		super(name);
	}

	public void addRole(Role role) {
		Assert.Arg.notNull(role, "role");
		addRole(role.getName());
	}

	public boolean isInRole(Role role) {
		Assert.Arg.notNull(role, "role");
		return isInRole(role.getName());
	}

	public boolean isOwner() {
		return isInRole(Role.OWNER);
	}

	public boolean isViewer() {
		return isInRole(Role.VIEWER);
	}

	public void setSessionPersisted(boolean persisted) {
		this.sessionPersisted = persisted;
	}

	public boolean hasSessionPersisted() {
		return this.sessionPersisted;
	}

	public boolean isAnonymous() {
		return this.anonymous;
	}

	public void setAnonymous(boolean anonymous) {
		this.anonymous = anonymous;
	}
}

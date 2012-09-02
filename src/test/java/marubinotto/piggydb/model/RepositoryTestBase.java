package marubinotto.piggydb.model;

import java.util.ArrayList;
import java.util.List;

import marubinotto.piggydb.model.auth.User;
import marubinotto.piggydb.model.enums.Role;

import org.junit.Before;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

@RunWith(Parameterized.class)
public abstract class RepositoryTestBase<T> {
	
	public static interface RepositoryFactory<T> {
		public T create() throws Exception;
	}
	
	private RepositoryFactory<T> factory;
	protected T object;
	
	private User plainUser = new User("plain");
	private User owner = new User("owner");
	private User viewer = new User("viewer");
	
	public RepositoryTestBase(RepositoryFactory<T> factory) {
		this.factory = factory;
	}

	@Before
	public void given() throws Exception {
		this.object = this.factory.create();
		this.owner.addRole(Role.OWNER);
		this.viewer.addRole(Role.VIEWER);
	}

	protected static List<Object[]> toParameters(Object ... factories) {	
		List<Object[]> parameters = new ArrayList<Object[]>();
		for (Object factory : factories) parameters.add(new Object[]{factory});
		return parameters;
	}
	
	public User getPlainUser() {
		return this.plainUser;
	}

	public User getOwner() {
		return this.owner;
	}

	public User getViewer() {
		return this.viewer;
	}
}

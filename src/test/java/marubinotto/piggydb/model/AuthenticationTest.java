package marubinotto.piggydb.model;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertNotNull;
import static marubinotto.piggydb.fixture.EntityFixtures.newFragmentWithTitleAndTags;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import marubinotto.piggydb.impl.InMemoryDatabase;
import marubinotto.piggydb.model.enums.Role;

import org.junit.Before;
import org.junit.Test;

public class AuthenticationTest {
	
	private Authentication object = new Authentication();

	private OwnerAuth ownerAuth = new OwnerAuth();
	private GlobalSetting globalSetting = new GlobalSetting.InMemory();
	
	private DefaultAuth defaultAuth = new DefaultAuth();
	private FragmentRepository fragmentRepository = 
		new InMemoryDatabase().getFragmentRepository();
	
	@Before
	public void given() throws Exception {
		this.ownerAuth.setGlobalSetting(this.globalSetting);
		this.defaultAuth.setFragmentRepository(this.fragmentRepository);
		
		this.object.setOwnerAuth(this.ownerAuth);
		this.object.setDefaultAuth(this.defaultAuth);
		
		this.fragmentRepository.register(
			newFragmentWithTitleAndTags("marubinotto", "#user"));
	}
	
	@Test
	public void invalidUser() throws Exception {
		User user = this.object.authenticate("foo", "bar");
		assertNull(user);
	}
	
	@Test
	public void owner() throws Exception {
		User user = this.object.authenticate("owner", "owner");
		
		assertThat(user.getName(), is("owner"));
		assertFalse(user.isAnonymous());
		assertTrue(user.isInRole(Role.OWNER));
		assertTrue(user.isInRole(Role.DEFAULT));
		assertFalse(user.isInRole(Role.VIEWER));
	}
	
	@Test
	public void guest() throws Exception {
		this.object.setEnableGuest(true);
		
		User user = this.object.authenticate("guest", "guest");
		
		assertThat(user.getName(), is("guest"));
		assertFalse(user.isAnonymous());
		assertFalse(user.isInRole(Role.OWNER));
		assertTrue(user.isInRole(Role.DEFAULT));
		assertFalse(user.isInRole(Role.VIEWER));
	}
	
	@Test
	public void normalUser() throws Exception {
		User user = this.object.authenticate("marubinotto", "marubinotto");
		
		assertThat(user.getName(), is("marubinotto"));
		assertFalse(user.isAnonymous());
		assertFalse(user.isInRole(Role.OWNER));
		assertTrue(user.isInRole(Role.DEFAULT));
		assertFalse(user.isInRole(Role.VIEWER));
	}
	
	@Test
	public void disabledAnonymousByDefault() throws Exception {
		assertNull(this.object.authenticateAsAnonymous());
	}
	
	@Test
	public void authenticateAsAnonymous() throws Exception {
		this.object.setEnableAnonymous(true);
		
		User user = this.object.authenticateAsAnonymous();
		
		assertNotNull(user);
		assertTrue(user.isAnonymous());
		assertEquals("anonymous", user.getName());
		assertFalse(user.isInRole(Role.OWNER));
		assertTrue(user.isInRole(Role.DEFAULT));
		assertTrue(user.isInRole(Role.VIEWER));
	}
}

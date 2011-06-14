package marubinotto.piggydb.model;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import marubinotto.piggydb.model.enums.Role;

import org.junit.Before;
import org.junit.Test;

public class OwnerAuthTest {

	private OwnerAuth object = new OwnerAuth();
	private GlobalSetting globalSetting = new GlobalSetting.InMemory();
	
	@Before
	public void given() {
		this.object.setGlobalSetting(this.globalSetting);
	}
	
// checkOwnerPassword
	
	@Test
	public void defaultPassword() throws Exception {
		assertTrue(this.object.validatePassword("owner"));
	}
	
	@Test
	public void invalidDefaultPassword() throws Exception {
		assertFalse(this.object.validatePassword("invalid-default-password"));
	}
	
	private static final String SSHA_pe00001 = 
		"{SSHA}KyTPouHDohrf6NSxhT3z8F7dsyDSTwlhJSfRfg==";
	
	@Test
	public void storedPassword() throws Exception {
		this.globalSetting.put("owner.password", SSHA_pe00001);
		
		assertTrue(this.object.validatePassword("pe00001"));
	}
	
	@Test
	public void invalidPassword() throws Exception {
		this.globalSetting.put("owner.password", SSHA_pe00001);
		
		assertFalse(this.object.validatePassword("invalid-password"));
	}
	
// authorizeAsOwner
	
	@Test
	public void authorizeWithDefaultPassword() throws Exception {
		// Given
		User user = new User("owner");
		assertFalse(user.isInRole(Role.OWNER));
		
		// When
		boolean result = this.object.authorizeAsOwner(user, "owner");
		
		// Then
		assertTrue(result);
		assertTrue(user.isInRole(Role.OWNER));
	}
	
	@Test
	public void authorizeWithInvalidUserName() throws Exception {
		// Given
		User user = new User("not-owner");
		
		// When
		boolean result = this.object.authorizeAsOwner(user, "owner");
		
		// Then
		assertFalse(result);
		assertFalse(user.isInRole(Role.OWNER));
	}
	
	@Test
	public void authorizeWithInvalidPassword() throws Exception {
		// Given
		User user = new User("owner");
		assertFalse(user.isInRole(Role.OWNER));
		
		// When
		boolean result = this.object.authorizeAsOwner(user, "invalid-password");
		
		// Then
		assertFalse(result);
		assertFalse(user.isInRole(Role.OWNER));		
	}
	
// changeOwnerPassword
	
	@Test
	public void createPassword() throws Exception {
		// Given
		assertNull(this.globalSetting.get("owner.password"));
		
		// When
		this.object.changePassword("password");

		// Then
		assertNotNull(this.globalSetting.get("owner.password"));
		assertTrue(this.object.validatePassword("password"));
	}
	
	@Test
	public void changePassword() throws Exception {
		// When
		this.object.changePassword("password");
		this.object.changePassword("changed-password");
		
		// Then
		assertTrue(this.object.validatePassword("changed-password"));	
	}
}

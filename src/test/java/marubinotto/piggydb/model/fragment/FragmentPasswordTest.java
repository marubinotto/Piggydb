package marubinotto.piggydb.model.fragment;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import marubinotto.piggydb.model.entity.RawFragment;

import org.junit.Before;
import org.junit.Test;

public class FragmentPasswordTest {

	private RawFragment object = new RawFragment();
	
	@Before
	public void given() {
		this.object.setTitle("marubinotto");
	}
	
	@Test
	public void invalidDefaultPassword() throws Exception {
		assertFalse(this.object.validatePassword("invalid-default-password"));
	}
	
	@Test
	public void defaultPassword() throws Exception {
		assertTrue(this.object.validatePassword("marubinotto"));
	}
	
	private static final String SSHA_pe00001 = 
		"{SSHA}KyTPouHDohrf6NSxhT3z8F7dsyDSTwlhJSfRfg==";
	
	@Test
	public void storedPassword() throws Exception {
		this.object.setPassword(SSHA_pe00001);
		assertTrue(this.object.validatePassword("pe00001"));
	}
	
	@Test
	public void invalidPassword() throws Exception {
		this.object.setPassword(SSHA_pe00001);
		assertFalse(this.object.validatePassword("invalid-password"));
	}
	
	@Test
	public void createPassword() throws Exception {
		this.object.changePassword("password");

		assertNotNull(this.object.getPassword());
		assertTrue(this.object.validatePassword("password"));
	}
	
	@Test
	public void changePassword() throws Exception {
		this.object.changePassword("password");
		this.object.changePassword("changed-password");
		
		assertTrue(this.object.validatePassword("changed-password"));	
	}
}

package marubinotto.piggydb.model;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertFalse;
import static marubinotto.piggydb.fixture.EntityFixtures.newFragmentWithTitleAndTags;
import static org.junit.Assert.assertTrue;
import marubinotto.piggydb.impl.jdbc.h2.InMemoryDatabase;
import marubinotto.piggydb.model.enums.Role;

import org.junit.Before;
import org.junit.Test;

public class DefaultAuthTest {

	private DefaultAuth object = new DefaultAuth();
	
	private FragmentRepository fragmentRepository = 
		new InMemoryDatabase().getFragmentRepository();
	private Long fragmentId;
	
	private User authorizedUser = new User("marubinotto");
	
	@Before
	public void given() throws Exception {
		this.object.setFragmentRepository(this.fragmentRepository);
		
		this.fragmentId = this.fragmentRepository.register(
			newFragmentWithTitleAndTags("marubinotto", "#user"));
		
		this.object.authorizeAsNormalUser(this.authorizedUser, "marubinotto");
	}
	
	@Test
	public void authorizeWithDefaultPassword() throws Exception {
		boolean result = this.object.authorizeAsNormalUser(
			new User("marubinotto"), "marubinotto");
		assertTrue(result);
	}
	
	@Test
	public void authorizeWithInvalidDefaultPassword() throws Exception {
		boolean result = this.object.authorizeAsNormalUser(
			new User("marubinotto"), "invalid-password");
		assertFalse(result);
	}
	
	@Test
	public void authorizeWithInvalidUserName() throws Exception {
		boolean result = this.object.authorizeAsNormalUser(
			new User("hoge"), "hoge");
		assertFalse(result);
	}
	
	@Test
	public void homeFragmentId() throws Exception {
		assertEquals(this.fragmentId, this.authorizedUser.homeFragmentId);
	}
	
	@Test
	public void role() throws Exception {
		assertTrue(this.authorizedUser.isInRole(Role.INTERNAL_USER));
	}
}

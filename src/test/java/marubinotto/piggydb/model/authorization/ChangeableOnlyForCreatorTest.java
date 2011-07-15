package marubinotto.piggydb.model.authorization;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertTrue;
import static junit.framework.Assert.fail;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import marubinotto.piggydb.model.User;
import marubinotto.piggydb.model.entity.RawEntity;
import marubinotto.piggydb.model.entity.RawFragment;
import marubinotto.piggydb.model.exception.AuthorizationException;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class ChangeableOnlyForCreatorTest extends AuthorizationTestBase {

	private RawFragment object = new RawFragment(getPlainUser());
	
	@Before
	public void given() throws Exception {
		super.given();
		RawEntity.changeableOnlyForCreator = true;
	}
	
	@After
	public void tearDown() {
		RawEntity.changeableOnlyForCreator = false;
	}
	
	// Can change

	@Test
	public void creatorCanChange() throws Exception {
		assertTrue(this.object.canChange(getPlainUser()));
	}

	@Test
	public void ownerCanChange() throws Exception {
		assertTrue(this.object.canChange(getOwner()));
	}

	@Test
	public void anotherCannotChange() throws Exception {
		assertFalse(this.object.canChange(new User("another")));
	}
	
	// Can delete

	@Test
	public void creatorCanDelete() throws Exception {
		assertTrue(this.object.canDelete(getPlainUser()));
	}

	@Test
	public void ownerCanDelete() throws Exception {
		assertTrue(this.object.canDelete(getOwner()));
	}

	@Test
	public void anotherCannotDelete() throws Exception {
		assertFalse(this.object.canDelete(new User("another")));
	}
	
	
	// Change the title
	
	@Test
	public void changeTitleByCreator() throws Exception {
		this.object.setTitleByUser("changed", getPlainUser());
	}
	
	@Test
	public void changeTitleByOwner() throws Exception {
		this.object.setTitleByUser("changed", getOwner());
	}
	
	@Test
	public void changeTitleByAnother() throws Exception {
		try {
			this.object.setTitleByUser("changed", new User("another"));
			fail();
		} 
		catch (AuthorizationException e) {
			assertEquals(AuthErrors.toChangeEntity(this.object), e);
		}
		assertNull(this.object.getTitle());
	}
	
	// Touch (with auth)
	
	@Test
	public void touchByCreator() throws Exception {
		this.object.touch(getPlainUser(), false);
	}
	
	@Test
	public void touchByOwner() throws Exception {
		this.object.touch(getOwner(), false);
	}
	
	@Test
	public void touchByAnother() throws Exception {
		try {
			this.object.touch(new User("another"), false);
			fail();
		} 
		catch (Exception e) {
			assertEquals(AuthErrors.toChangeEntity(this.object), e);
		}
		assertEquals(getPlainUser().getName(), this.object.getLastUpdaterOrCreator());
	}
}

package marubinotto.piggydb.model.authorization;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import marubinotto.piggydb.model.Tag;
import marubinotto.piggydb.model.User;
import marubinotto.piggydb.model.entity.RawFragment;
import marubinotto.piggydb.model.entity.RawTag;

import org.junit.Before;
import org.junit.Test;

public class FragmentTest extends AuthorizationTestBase {
	
	private RawFragment object = new RawFragment(getPlainUser());
	
	@Before
	public void given() throws Exception {
		super.given();
		this.object.setId(1L);
	}
	
	// Can change
	
	@Test
	public void creatorCanChange() throws Exception {
		assertTrue(this.object.canChange(getPlainUser()));
	}
	
	@Test
	public void anotherCanChange() throws Exception {
		assertTrue(this.object.canChange(new User("another")));
	}
	
	@Test
	public void viewerCannotChange() throws Exception {
		assertFalse(this.object.canChange(getViewer()));
	}
	
	
	// Can delete
	
	@Test
	public void creatorCanDelete() throws Exception {
		assertTrue(this.object.canDelete(getPlainUser()));
	}
	
	@Test
	public void anotherCanDelete() throws Exception {
		assertTrue(this.object.canDelete(new User("another")));
	}
	
	@Test
	public void viewerCannotDelete() throws Exception {
		assertFalse(this.object.canDelete(getViewer()));
	}
		
	
	// Add #trash tag
	
	@Test
	public void creatorCanAddTrashTag() throws Exception {
		this.object.addTagByUser(new RawTag(Tag.NAME_TRASH), getPlainUser());
	}
	
	@Test
	public void anotherCanAddTrashTag() throws Exception {
		this.object.addTagByUser(new RawTag(Tag.NAME_TRASH), new User("another"));
	}
}

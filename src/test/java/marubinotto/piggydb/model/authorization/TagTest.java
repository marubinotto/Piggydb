package marubinotto.piggydb.model.authorization;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertTrue;
import static junit.framework.Assert.fail;
import marubinotto.piggydb.model.AuthorizationException;
import marubinotto.piggydb.model.Tag;
import marubinotto.piggydb.model.entity.RawTag;

import org.junit.Before;
import org.junit.Test;

public class TagTest extends AuthorizationTestBase {
	
	private RawTag object = new RawTag("tag");
	
	@Before
	public void given() throws Exception {
		super.given();
	}
	
	// Authorizes
	
	@Test
	public void shouldAuthorizePlainUser() throws Exception {
		assertTrue(this.object.authorizes(getPlainUser()));
	}
	
	@Test
	public void shouldNotAuthorizeViewer() throws Exception {
		assertFalse(this.object.authorizes(getViewer()));
	}
	
	
	// Can delete
	
	@Test
	public void plainUserCanDelete() throws Exception {
		assertTrue(this.object.canDelete(getOwner()));
	}
	
	@Test
	public void viewerCannotDelete() throws Exception {
		assertFalse(this.object.canDelete(getViewer()));
	}
	
	
	// Rename
	
	@Test
	public void plainUserCanRename() throws Exception {
		this.object.setNameByUser("hogehoge", getPlainUser());
	}
	
	@Test
	public void viewerCannotRename() throws Exception {
		cannotRename(this.object, "hogehoge", getViewer(), this.object.getName());
	}

	
	// Add #trash tag
	
	@Test
	public void ownerCanAddTrashTag() throws Exception {
		this.object.addTagByUser(new RawTag(Tag.NAME_TRASH), getOwner());
	}
	
	@Test
	public void nonOwnerCannotAddTrashTag() throws Exception {
		try {
			this.object.addTagByUser(new RawTag(Tag.NAME_TRASH), getPlainUser());
			fail();
		} 
		catch (AuthorizationException e) {
			assertEquals(AuthErrors.toExtendTrash(), e);
		}
		assertFalse(this.object.getClassification().containsTagName(Tag.NAME_TRASH));
	}

	@Test
	public void nonOwnerCannotAddSubTrashTag() throws Exception {
		RawTag tag = new RawTag("temp");
		tag.getMutableClassification().addTag(new RawTag(Tag.NAME_TRASH));
		try {
			this.object.addTagByUser(tag, getPlainUser());
			fail();
		} 
		catch (AuthorizationException e) {
			assertEquals(AuthErrors.toExtendTrash(), e);
		}
		assertFalse(this.object.getClassification().containsTagName(tag.getName()));
	}
}

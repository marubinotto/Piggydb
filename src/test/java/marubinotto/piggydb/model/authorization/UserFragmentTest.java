package marubinotto.piggydb.model.authorization;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertNull;
import static junit.framework.Assert.assertTrue;
import static junit.framework.Assert.fail;
import marubinotto.piggydb.model.AuthorizationException;
import marubinotto.piggydb.model.Fragment;
import marubinotto.piggydb.model.Tag;
import marubinotto.piggydb.model.User;
import marubinotto.piggydb.model.entity.RawFragment;
import marubinotto.piggydb.model.entity.RawTag;
import marubinotto.util.time.DateTime;

import org.junit.Before;
import org.junit.Test;

public class UserFragmentTest extends AuthorizationTestBase {

	private RawFragment userFragment = new RawFragment();
	private RawFragment subUserFragment = new RawFragment();
	
	private User targetUser = new User("akane");
	private User anotherUser = new User("haruki");
	
	private RawTag userTag = new RawTag(Tag.NAME_USER);
	private RawTag subUserTag = new RawTag("sub-user");
	private RawTag anotherTag = new RawTag("idiot"); 
	
	private String originalUpdater;
	private DateTime originalUpdateDateTime;
	
	@Before
	public void given() throws Exception {
		super.given();
		
		this.subUserTag.getMutableClassification().addTag(this.userTag);
		
		this.userFragment.setId(1L);
		this.userFragment.setTitle(this.targetUser.getName());
		this.userFragment.addTagByUser(this.userTag, getOwner());
		this.userFragment.addTagByUser(this.anotherTag, getOwner());
		
		this.originalUpdater = this.userFragment.getUpdater();
		this.originalUpdateDateTime = this.userFragment.getUpdateDatetime();
		
		this.subUserFragment.setId(2L);
		this.subUserFragment.setTitle(this.targetUser.getName());
		this.subUserFragment.addTagByUser(this.subUserTag, getOwner());
	}
	
	private void assertUserFragmentNotUpdated() {
		assertEquals(this.originalUpdater, this.userFragment.getUpdater());
		assertEquals(this.originalUpdateDateTime, this.userFragment.getUpdateDatetime());
	}
	
	// Remove #user tag
	
	@Test
	public void ownerCanRemoveUserTag() throws Exception {
		this.userFragment.removeTagByUser(this.userTag.getName(), getOwner());
	}
	
	@Test
	public void nonOwnerCannotRemoveUserTag() throws Exception {
		cannotRemoveTag(this.userFragment, this.userTag.getName(), this.targetUser,
			AuthErrors.forTag(this.userTag));
	}
	
	@Test
	public void ownerCanRemoveSubUserTag() throws Exception {
		this.subUserFragment.removeTagByUser(this.subUserTag.getName(), getOwner());
	}

	@Test
	public void nonOwnerCannotRemoveSubUserTag() throws Exception {
		cannotRemoveTag(this.subUserFragment, this.subUserTag.getName(), this.targetUser,
			AuthErrors.forTag(this.subUserTag));
	}
	
	
	// Add #trash tag
	
	@Test
	public void ownerCanAddTrashTag() throws Exception {
		this.userFragment.addTagByUser(new RawTag(Tag.NAME_TRASH), getOwner());
	}
	
	@Test
	public void nonOwnerCannotAddTrashTag() throws Exception {
		try {
			this.userFragment.addTagByUser(new RawTag(Tag.NAME_TRASH), this.targetUser);
			fail();
		}
		catch (AuthorizationException e) {
			assertEquals(AuthErrors.toDeleteFragment(this.userFragment), e);
		}
		assertFalse(this.userFragment.getClassification().containsTagName(Tag.NAME_TRASH));
	}
	
	
	// Change the title
	
	@Test
	public void ownerCanChangeTitle() throws Exception {
		this.userFragment.setTitleByUser("marubin", getOwner());
	}
	
	@Test
	public void nonOwnerCannotChangeTitle() throws Exception {
		try {
			this.userFragment.setTitleByUser("marubin", this.targetUser);
			fail();
		}
		catch (AuthorizationException e) {
			assertEquals(AuthErrors.toChangeFragment(this.userFragment), e);
		}
		assertUserFragmentNotUpdated();
		assertEquals(this.targetUser.getName(), this.userFragment.getTitle());
	}
	
	@Test
	public void nonOwnerCanSetSameTitle() throws Exception {
		this.userFragment.setTitleByUser(this.targetUser.getName(), this.targetUser);
		assertUserFragmentNotUpdated();
	}
	
	
	// Change the content
	
	@Test
	public void ownerCanChangeContent() throws Exception {
		this.userFragment.setContentByUser("hogehoge", getOwner());
	}
	
	@Test
	public void targetUserCanChangeContent() throws Exception {
		this.userFragment.setContentByUser("hogehoge", this.targetUser);
	}
	
	@Test
	public void nonTargetUserCannotChangeContent() throws Exception {
		try {
			this.userFragment.setContentByUser("hogehoge", this.anotherUser);
			fail();
		}
		catch (AuthorizationException e) {
			assertEquals(AuthErrors.toChangeFragment(this.userFragment), e);
		}
		assertUserFragmentNotUpdated();
		assertNull(this.userFragment.getContent());
	}
	
	@Test
	public void nonTargetUserCanSetSameContent() throws Exception {
		this.userFragment.setContentByUser(null, this.anotherUser);
		assertUserFragmentNotUpdated();
	}
	
	
	// Add a tag
	
	@Test
	public void ownerCanAddTag() throws Exception {
		this.userFragment.addTagByUser(new RawTag("tag"), getOwner());
	}
	
	@Test
	public void targetUserCanAddTag() throws Exception {
		this.userFragment.addTagByUser(new RawTag("tag"), this.targetUser);
	}
	
	@Test
	public void nonTargetUserCannotAddTag() throws Exception {
		try {
			this.userFragment.addTagByUser(new RawTag("tag"), this.anotherUser);
			fail();
		} 
		catch (AuthorizationException e) {
			assertEquals(AuthErrors.toChangeFragment(this.userFragment), e);
		}
		assertFalse(this.userFragment.getClassification().containsTagName("tag"));
	}
	
	// Remove a tag
	
	@Test
	public void ownerCanRemoveTag() throws Exception {
		this.userFragment.removeTagByUser(this.anotherTag.getName(), getOwner());
	}
	
	@Test
	public void targetUserCanRemoveTag() throws Exception {
		this.userFragment.removeTagByUser(this.anotherTag.getName(), this.targetUser);
	}
	
	@Test
	public void nonTargetUserCannotRemoveTag() throws Exception {
		cannotRemoveTag(this.userFragment, this.anotherTag.getName(), this.anotherUser,
			AuthErrors.toChangeFragment(this.userFragment));
	}


// Private
	
	private static void cannotRemoveTag(
		Fragment fragment,
		String tagName, 
		User user,
		AuthorizationException expectedError)
	throws Exception {
		try {
			fragment.removeTagByUser(tagName, user);
			fail();
		} 
		catch (AuthorizationException e) {
			assertEquals(expectedError, e);
		}
		assertTrue(fragment.getClassification().containsTagName(tagName));
	}
}

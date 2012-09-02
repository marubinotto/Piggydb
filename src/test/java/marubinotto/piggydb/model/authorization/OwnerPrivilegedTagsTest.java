package marubinotto.piggydb.model.authorization;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertTrue;
import static junit.framework.Assert.fail;

import java.util.ArrayList;
import java.util.List;

import marubinotto.piggydb.model.Classifiable;
import marubinotto.piggydb.model.Tag;
import marubinotto.piggydb.model.auth.User;
import marubinotto.piggydb.model.entity.RawFragment;
import marubinotto.piggydb.model.entity.RawTag;
import marubinotto.piggydb.model.exception.AuthorizationException;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

@RunWith(Parameterized.class)
public class OwnerPrivilegedTagsTest extends AuthorizationTestBase {

	private String targetTagName;
	
	private RawTag tag;
	private RawTag subTag = new RawTag("sub");
	
	private RawFragment fragment = new RawFragment();
	
	public OwnerPrivilegedTagsTest(String tagName) {
		this.targetTagName = tagName;
	}
	
	@Parameters
	public static List<Object[]> tagNames() {
		List<Object[]> tagNames = new ArrayList<Object[]>();
		tagNames.add(new Object[]{Tag.NAME_USER});
		tagNames.add(new Object[]{Tag.NAME_PUBLIC});
		tagNames.add(new Object[]{Tag.NAME_HOME});
		tagNames.add(new Object[]{Tag.NAME_BOOKMARK});
		return tagNames;
	}

	@Before
	public void given() throws Exception {
		super.given();
		
		this.tag = new RawTag(this.targetTagName);
		this.subTag.getMutableClassification().addTag(this.tag);
	}
	
	// Authorizes
	
	@Test
	public void shouldAuthorizeOwner() throws Exception {
		assertTrue(this.tag.authorizes(getOwner()));
	}
	
	@Test
	public void shouldNotAuthorizeNonOwner() throws Exception {
		assertFalse(this.tag.authorizes(getPlainUser()));
	}
	
	
	// Create
	
	@Test
	public void ownerCanCreate() throws Exception {
		new RawTag(this.targetTagName, getOwner());
	}
	
	@Test
	public void nonOwnerCannotCreate() throws Exception {
		try {
			new RawTag(this.targetTagName, getPlainUser());
			fail();
		} 
		catch (AuthorizationException e) {
			assertEquals(AuthErrors.forTag(this.targetTagName), e);
		}
	}
	
	
	// Rename
	
	@Test
	public void ownerCanRename() throws Exception {
		this.tag.setNameByUser("hogehoge", getOwner());
	}
	
	@Test
	public void nonOwnerCannotRename() throws Exception {
		cannotRename(this.tag, "hogehoge", getPlainUser(), this.tag.getName());
	}
	
	@Test
	public void ownerCanRenameSubTag() throws Exception {
		this.subTag.setNameByUser("hogehoge", getOwner());
	}
	
	@Test
	public void nonOwnerCannotRenameSubTag() throws Exception {
		cannotRename(this.subTag, "hogehoge", getPlainUser(), this.subTag.getName());
	}
	
	@Test
	public void nonOwnerCanSetSameName() throws Exception {
		this.tag.setNameByUser(this.targetTagName, getPlainUser());
	}
	
	
	// Rename to
	
	@Test
	public void ownerCanRenameTo() throws Exception {
		Tag tag = new RawTag("tag");
		tag.setNameByUser(this.targetTagName, getOwner());
	}
	
	@Test
	public void nonOwnerCannotRenameTo() throws Exception {
		Tag tag = new RawTag("tag");
		cannotRename(tag, this.targetTagName, getPlainUser(), this.targetTagName);
	}

	
	// Can delete
	
	@Test
	public void ownerCanDelete() throws Exception {
		assertTrue(this.tag.canDelete(getOwner()));
	}
	
	@Test
	public void nonOwnerCannotDelete() throws Exception {
		assertFalse(this.tag.canDelete(getPlainUser()));
	}
	
	@Test
	public void ownerCanDeleteSubTag() throws Exception {
		assertTrue(this.subTag.canDelete(getOwner()));
	}

	@Test
	public void nonOwnerCannotDeleteSubTag() throws Exception {
		assertFalse(this.subTag.canDelete(getPlainUser()));
	}
	
	
	// Add a tag
	
	@Test
	public void ownerCanAddTag() throws Exception {
		this.tag.addTagByUser(new RawTag("tag"), getOwner());
	}
	
	@Test
	public void nonOwnerCannotAddTag() throws Exception {
		try {
			this.tag.addTagByUser(new RawTag("tag"), getPlainUser());
			fail();
		} 
		catch (AuthorizationException e) {
			assertEquals(AuthErrors.forTag(this.tag.getName()), e);
		}
		assertFalse(this.tag.getClassification().containsTagName("tag"));
	}
	
	
	// Add to a fragment

	@Test
	public void ownerCanAddToFragment() throws Exception {
		this.fragment.addTagByUser(this.tag, getOwner());
	}
	
	@Test
	public void nonOwnerCannotAddToFragment() throws Exception {
		cannotAddTag(this.tag, this.fragment, getPlainUser());
	}

	@Test
	public void ownerCanAddSubTagToFragment() throws Exception {
		this.fragment.addTagByUser(this.subTag, getOwner());
	}
	
	@Test
	public void nonOwnerCannotAddSubTagToFragment() throws Exception {
		cannotAddTag(this.subTag, this.fragment, getPlainUser());
	}
	
	private static void cannotAddTag(Tag privilegedTag, Classifiable classifiable, User user) 
	throws Exception {
		try {
			classifiable.addTagByUser(privilegedTag, user);
			fail();
		} 
		catch (AuthorizationException e) {
			assertEquals(AuthErrors.forTag(privilegedTag), e);
		}
		assertFalse(classifiable.getClassification().containsTagName(privilegedTag.getName()));
	}
}

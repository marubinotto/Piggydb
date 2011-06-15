package marubinotto.piggydb.model.authorization;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertFalse;
import static marubinotto.piggydb.model.Assert.assertClassificationEquals;
import static marubinotto.util.CollectionUtils.list;
import static marubinotto.util.CollectionUtils.set;
import static org.junit.Assert.fail;
import marubinotto.piggydb.impl.jdbc.h2.InMemoryDatabase;
import marubinotto.piggydb.model.AuthorizationException;
import marubinotto.piggydb.model.Tag;
import marubinotto.piggydb.model.TagRepository;
import marubinotto.piggydb.model.entity.RawFragment;

import org.junit.Before;
import org.junit.Test;

public class ClassifiableTest extends AuthorizationTestBase {
	
	private RawFragment classifiable = new RawFragment();
	private TagRepository tagRepository = new InMemoryDatabase().getTagRepository();
	private Tag privilegedTag;
	
	@Before
	public void given() throws Exception {
		super.given();
		
		this.privilegedTag = this.tagRepository.newInstance("#user", getOwner());
		this.tagRepository.register(this.privilegedTag);
	}
	
	@Test
	public void ownerCanAddPrivilegedTag() throws Exception {
		this.classifiable.addTagByUser(this.privilegedTag, getOwner());
	}
	
	@Test
	public void nonOwnerCannotAddPrivilegedTag() throws Exception {
		try {
			this.classifiable.addTagByUser(this.privilegedTag, getPlainUser());
			fail();
		} 
		catch (AuthorizationException e) {
			assertEquals(AuthErrors.forTag(this.privilegedTag), e);
		}
		assertFalse(this.classifiable.getClassification().containsTagName(this.privilegedTag.getName()));
	}
	
	@Test
	public void nonOwnerCannotAddPrivilegedTag_updateTags() throws Exception {
		try {
			this.classifiable.updateTagsByUser(
				list(this.privilegedTag.getName()), this.tagRepository, getPlainUser());
			fail();
		} 
		catch (AuthorizationException e) {
			assertEquals(AuthErrors.forTag(this.privilegedTag), e);
		}
		assertFalse(this.classifiable.getClassification().containsTagName(this.privilegedTag.getName()));
	}
	
	@Test
	public void nonOwnerCanKeepPrivilegedTag() throws Exception {
		this.classifiable.addTagByUser(this.privilegedTag, getOwner());
		this.classifiable.addTagByUser(this.privilegedTag, getPlainUser());
		
		assertClassificationEquals(
			set(this.privilegedTag.getName()),  
			this.classifiable.getClassification());
	}

	@Test
	public void nonOwnerCanKeepPrivilegedTag_updateTags() throws Exception {
		this.classifiable.addTagByUser(this.privilegedTag, getOwner());
		
		this.classifiable.updateTagsByUser(
			list(this.privilegedTag.getName()), this.tagRepository, getPlainUser());
		
		assertClassificationEquals(
			set(this.privilegedTag.getName()),  
			this.classifiable.getClassification());
	}
}

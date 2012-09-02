package marubinotto.piggydb.model.tags;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.Iterator;

import marubinotto.piggydb.model.Tag;
import marubinotto.piggydb.model.TagRepository;
import marubinotto.piggydb.model.auth.User;
import marubinotto.util.paging.Page;
import marubinotto.util.time.DateTime;

import org.junit.Test;

public class DefaultTest extends TagRepositoryTestBase {
	
	public DefaultTest(RepositoryFactory<TagRepository> factory) {
		super(factory);
	}
	
	@Test
	public void sizeShouldBeZero() throws Exception {
		assertEquals(0, this.object.size());
	}
	
	@Test
	public void taggingsCountShouldBeZeroOrNull() throws Exception {
		Long count = this.object.countTaggings();
		if (count != null) assertEquals(0, count.longValue());
	}
	
	@Test
	public void newInstance() throws Exception {
		User user = getPlainUser();
		Tag tag = this.object.newInstance("name", user);
		
		assertEquals("name", tag.getName());
		assertEquals(user.getName(), tag.getCreator());
		assertNull(tag.getUpdater());
	}
	
	@Test
	public void register() throws Exception {
		// Given
		DateTime registerDateTime = new DateTime(2008, 1, 1);
		DateTime.setCurrentTimeForTest(registerDateTime);

		// When
		Tag tag = this.object.newInstance("name", getPlainUser());
		long tagId = this.object.register(tag);
		
		// Then
		assertEquals(tagId, tag.getId().longValue());
		assertEquals(registerDateTime, tag.getCreationDatetime());
		assertEquals(registerDateTime, tag.getUpdateDatetime());

		// The post conditions for the repository is described by OneTagTest
	}
	
	@Test
	public void getRecentChanges() throws Exception {
		Page<Tag> results = this.object.getRecentChanges(1, 0);
		assertTrue(results.isEmpty());
	}

	@Test
	public void getRootTags() throws Exception {
		Page<Tag> results = this.object.getRootTags(1, 0);
		assertTrue(results.isEmpty());
	}
	
	@Test
	public void iterateAllTagNames() throws Exception {
		Iterator<String> tagNames = this.object.iterateAllTagNames();
		assertFalse(tagNames.hasNext());
	}
}

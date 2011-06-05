package marubinotto.piggydb.model.tags;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import marubinotto.piggydb.model.Tag;
import marubinotto.piggydb.model.TagRepository;
import marubinotto.util.paging.Page;
import marubinotto.util.time.DateTime;

import org.junit.Before;
import org.junit.Test;

/**
 * There's no need to get classifications for the time being.
 */
public class GetRecentChangesTest extends TagRepositoryTestBase {
	
	public GetRecentChangesTest(RepositoryFactory<TagRepository> factory) {
		super(factory);
	}

	@Before
	public void given() throws Exception {
		super.given();
		
		DateTime.setCurrentTimeForTest(new DateTime(2008, 1, 1));
		this.object.register(newTag("pingpong"));
		
		DateTime.setCurrentTimeForTest(new DateTime(2008, 1, 2));
		this.object.register(newTag("soccer"));
		
		DateTime.setCurrentTimeForTest(null);
	}
	
	@Test
	public void orderedByUpdateDateDesc() throws Exception {
		// When
		Page<Tag> page = this.object.getRecentChanges(3, 0);
		
		// Then
		assertEquals(2, page.size());
		assertEquals("soccer", page.get(0).getName());
		assertEquals("pingpong", page.get(1).getName());
	}
	
	@Test
	public void orderedByUpdateDateDesc_oneUpdated() throws Exception {
		// Given
		DateTime.setCurrentTimeForTest(new DateTime(2008, 1, 3));
		Tag tag = this.object.getByName("pingpong");
		tag.setNameByUser("table tennis", getPlainUser());
		this.object.update(tag);
		
		// When
		Page<Tag> page = this.object.getRecentChanges(3, 0);
		
		// Then
		assertEquals(2, page.size());
		assertEquals("table tennis", page.get(0).getName());
		assertEquals("soccer", page.get(1).getName());
	}
	
	@Test
	public void paging() throws Exception {
		// When
		Page<Tag> page1 = this.object.getRecentChanges(1, 0);
		Page<Tag> page2 = this.object.getRecentChanges(1, 1);
		
		// Then
		assertEquals(1, page1.size());
		assertEquals(2, page1.getTotalSize());
		assertTrue(page1.isFirstPage());
		
		assertEquals(1, page2.size());
		assertEquals(2, page2.getTotalSize());
		assertTrue(page2.isLastPage());
	}
}

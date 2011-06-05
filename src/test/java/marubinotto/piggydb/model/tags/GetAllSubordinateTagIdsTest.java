package marubinotto.piggydb.model.tags;

import static marubinotto.util.CollectionUtils.set;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Set;

import marubinotto.piggydb.model.TagRepository;

import org.junit.Before;
import org.junit.Test;

public class GetAllSubordinateTagIdsTest extends TagRepositoryTestBase {
	
	private Long tagId_software;
	private Long tagId_agile;
	private Long tagId_xp;
	private Long tagId_literature;
	private Long tagId_haruki;
	
	public GetAllSubordinateTagIdsTest(RepositoryFactory<TagRepository> factory) {
		super(factory);
	}

	@Before
	public void given() throws Exception {
		super.given();
		
		this.tagId_software = this.object.register(newTag("software"));
		this.tagId_agile = this.object.register(newTagWithTags("agile", "software"));
		this.tagId_xp = this.object.register(newTagWithTags("eXtreme Programming", "agile"));
		
		this.tagId_literature = this.object.register(newTag("literature"));
		this.tagId_haruki = this.object.register(newTagWithTags("Haruki Murakami", "literature"));
	}
	
	@Test
	public void getForOneTag() throws Exception {
		// When
		Set<Long> tagIds = this.object.getAllSubordinateTagIds(
			set(this.tagId_software));
		
		// Then
		assertEquals(2, tagIds.size());
		assertTrue(tagIds.contains(this.tagId_agile));
		assertTrue(tagIds.contains(this.tagId_xp));
	}
	
	@Test
	public void getForTwoTags() throws Exception {
		// When
		Set<Long> tagIds = this.object.getAllSubordinateTagIds(
			set(this.tagId_software, this.tagId_literature));
		
		// Then
		assertEquals(3, tagIds.size());
		assertTrue(tagIds.contains(this.tagId_agile));
		assertTrue(tagIds.contains(this.tagId_xp));
		assertTrue(tagIds.contains(this.tagId_haruki));
	}
}

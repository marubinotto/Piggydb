package marubinotto.piggydb.model.classification;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.Iterator;
import java.util.Set;

import marubinotto.piggydb.model.MutableClassification;
import marubinotto.piggydb.model.Tag;
import marubinotto.piggydb.model.entity.RawTag;

import org.junit.Before;
import org.junit.Test;

public class OneTagTest {

	private MutableClassification object = new MutableClassification();
	private RawTag tag = new RawTag("tag", 1); 
	
	@Before
	public void given() throws Exception {
		this.object.addTag(this.tag);
	}
	
	@Test
	public void sizeShouldBeOne() throws Exception {
		assertEquals(1, this.object.size());
	}
	
	@Test
	public void shouldNotBeEmpty() throws Exception {
		assertFalse(this.object.isEmpty());
	}
	
	@Test
	public void getTagIterator() throws Exception {
		Iterator<Tag> iterator = this.object.getTagIterator();
		assertEquals("tag", iterator.next().getName());
	}
	
	@Test
	public void getTagByName() throws Exception {
		Tag tag = this.object.getTag("tag");

		assertNotNull(tag);
		assertEquals(1, tag.getId().longValue());
		assertEquals("tag", tag.getName());
	}

	@Test
	public void shouldKnowWhetherSpecifiedNameIsContained() throws Exception {
		assertTrue(this.object.containsTagName("tag"));
		assertFalse(this.object.containsTagName("not-such-tag"));
	}
	
	@Test
	public void shouldKnowWhetherSpecifiedIdIsContained() throws Exception {
		assertTrue(this.object.containsTagId(1));
		assertFalse(this.object.containsTagId(2));
	}
	
	@Test
	public void shouldKnowWhetherAnySpecifiedIdsAreContained() throws Exception {
		assertTrue(this.object.containsAny(Arrays.asList(1L, 2L)));
		assertFalse(this.object.containsAny(Arrays.asList(2L, 3L)));
	}
	
	@Test
	public void removeTag() throws Exception {
		this.object.removeTag("tag");
		
		assertEquals(0, this.object.size());
		assertFalse(this.object.containsTagName("tag"));
	}
	
	@Test
	public void replaceTagIfSameNameExists() throws Exception {
		this.object.addTag(new RawTag("tag", 2));
		
		assertEquals(1, this.object.size());
		assertTrue(this.object.containsTagName("tag"));
		assertFalse(this.object.containsTagId(1));
		assertTrue(this.object.containsTagId(2));
	}
	
	@Test
	public void getTagNames() throws Exception {
		Set<String> tagNames = this.object.getTagNames();
		
		assertEquals(1, tagNames.size());
		assertTrue(tagNames.contains("tag"));
	}
	
	@Test
	public void shouldBeSubordinateOfAddedTag() throws Exception {
		assertTrue(this.object.isSubordinateOf("tag"));
	}
	
	@Test
	public void shouldNotBeSubordinateOfUnrelatedTag() throws Exception {
		assertFalse(this.object.isSubordinateOf("unrelated"));
	}
	
	@Test
	public void shouldBeSubordinateOfGrandParentTag() throws Exception {
		this.tag.getMutableClassification().addTag(new RawTag("tag-of-tag"));

		assertTrue(this.object.isSubordinateOf("tag-of-tag"));
	}
	
	@Test
	public void shouldBeInSameHierarchyOfAddedTag() throws Exception {
		assertTrue(this.object.isInSameHierarchyOf(this.tag));
	}
	
	@Test
	public void shouldBeInSameHierarchyOfGrandParentTag() throws Exception {
		RawTag tagOfTag = new RawTag("tag-of-tag");
		this.tag.getMutableClassification().addTag(tagOfTag);
		
		assertTrue(this.object.isInSameHierarchyOf(tagOfTag));
	}
}

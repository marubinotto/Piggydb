package marubinotto.piggydb.model.tag;

import static org.junit.Assert.*;

import marubinotto.piggydb.model.entity.RawTag;
import marubinotto.piggydb.model.exception.InvalidTaggingException;

import org.junit.Test;

public class HierarchyTest {

	@Test
	public void sameTagShouldBeInSameHierarchy() throws Exception {
		RawTag tag = new RawTag("tag");

		assertTrue(tag.getClassification().isInSameHierarchyOf(tag));
	}
	
	@Test
	public void parentTagShouldBeInSameHierarchy() throws Exception {
		RawTag techTag = new RawTag("tech");
		RawTag javaTag = new RawTag("java");
		javaTag.getMutableClassification().addTag(techTag);
		
		assertTrue(techTag.getClassification().isInSameHierarchyOf(javaTag));
	}
	
	@Test
	public void notInSameHierarchy() throws Exception {
		RawTag techTag = new RawTag("tech");
		RawTag javaTag = new RawTag("java");
		javaTag.getMutableClassification().addTag(techTag);
		
		assertFalse(techTag.getClassification().isInSameHierarchyOf(new RawTag("ruby")));
	}
	
	/**
	 * To avoid a tagging loop.
	 */
	@Test(expected=InvalidTaggingException.class)
	public void shouldThrowExceptionWhenAddTagToItself() throws Exception {
		// Given
		RawTag tag = new RawTag("tag");
		
		// When
		tag.getMutableClassification().addTag(tag);
	}
	
	/**
	 * To avoid a tagging loop.
	 */
	@Test(expected=InvalidTaggingException.class)
	public void shouldThrowExceptionWhenAddSubordinateTag() throws Exception {
		// Given
		RawTag techTag = new RawTag("tech");
		RawTag javaTag = new RawTag("java");
		javaTag.getMutableClassification().addTag(techTag);
		
		// When
		techTag.getMutableClassification().addTag(javaTag);
	}
	
	@Test
	public void classifiedAsItself() throws Exception {
		RawTag tag = new RawTag("tag");
		assertTrue(tag.isClassifiedAs("tag"));
	}
	
	@Test
	public void notClassifiedAsTagNotInSameHierarchy() throws Exception {
		RawTag tag = new RawTag("tag");
		assertFalse(tag.isClassifiedAs("foo"));
	}
	
	@Test
	public void classifiedAsSuperordinateTag() throws Exception {
		RawTag techTag = new RawTag("tech");
		RawTag javaTag = new RawTag("java");
		javaTag.getMutableClassification().addTag(techTag);
		
		assertTrue(javaTag.isClassifiedAs("tech"));
	}
}

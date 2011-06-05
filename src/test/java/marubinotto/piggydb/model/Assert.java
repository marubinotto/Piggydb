package marubinotto.piggydb.model;

import static junit.framework.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Map;
import java.util.Set;

public class Assert {

	public static void assertClassificationEquals(Set<String> tags, Classification classification) {
		assertEquals("Classification size", tags.size(), classification.size());
		for (String tagName : tags) {
			assertTrue("Should include tag: " + tagName, classification.containsTagName(tagName));
		}
	}
	
	@SuppressWarnings("unchecked")
	public static void assertClassificationEquals(Map tags, Classification classification) {
		assertEquals(
			"Classification size: " + classification.getClassifiable(), 
			tags.size(), classification.size());
		for (Object tagName : tags.keySet()) {
			Tag tag = classification.getTag((String)tagName);
			assertTrue(
				classification.getClassifiable() + " should be tagged by: " + tagName, 
				tag != null);
			Map parentTags = (Map)tags.get(tagName);
			if (parentTags != null) {
				assertClassificationEquals(parentTags, tag.getClassification());
			}
			else {
				assertEquals(
					"Classification size should be zero: " + tag,
					0, tag.getClassification().size());
			}
		}
	}
}

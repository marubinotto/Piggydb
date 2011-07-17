package marubinotto.piggydb.ui.page.control;

import static marubinotto.piggydb.fixture.EntityFixtures.tagWithId;
import static org.junit.Assert.*;

import marubinotto.piggydb.model.Tag;
import marubinotto.piggydb.model.entity.RawTag;
import marubinotto.piggydb.ui.page.control.TagTree;
import net.sf.click.extras.tree.TreeNode;

import org.junit.Test;

public class TagTreeToStringTest {
	
	private void tagTreeStringShouldBeEqualsStringOfItsOrigin(Tag origin) {
		TreeNode tree = TagTree.buildTagTree(origin);
		String result = TagTree.toString(tree);
		assertEquals(origin.getClassification().toString(), result);
	}
	
	@Test
	public void emptyTree() throws Exception {
		Tag rootTag = tagWithId("root", 1);
		tagTreeStringShouldBeEqualsStringOfItsOrigin(rootTag);
	}

	@Test
	public void twoLevelTree() throws Exception {
		RawTag techTag = tagWithId("tech", 1);
		RawTag javaTag = tagWithId("java", 2);
		javaTag.getMutableClassification().addTag(techTag);

		RawTag rootTag = tagWithId("root", 3);
		rootTag.getMutableClassification().addTag(javaTag);
		
		tagTreeStringShouldBeEqualsStringOfItsOrigin(rootTag);
	}
	
	@Test
	public void pluralSiblings() throws Exception {
		RawTag rootTag = tagWithId("root", 1);
		rootTag.getMutableClassification().addTag(tagWithId("aaa", 2));
		rootTag.getMutableClassification().addTag(tagWithId("bbb", 3));
		rootTag.getMutableClassification().addTag(tagWithId("ccc", 4));
		
		tagTreeStringShouldBeEqualsStringOfItsOrigin(rootTag);
	}
	
	@Test
	public void pluralSiblingsInReversedOrder() throws Exception {
		RawTag rootTag = tagWithId("root", 1);
		rootTag.getMutableClassification().addTag(tagWithId("ccc", 2));
		rootTag.getMutableClassification().addTag(tagWithId("bbb", 3));
		rootTag.getMutableClassification().addTag(tagWithId("aaa", 4));

		tagTreeStringShouldBeEqualsStringOfItsOrigin(rootTag);
	}
}

package marubinotto.piggydb.presentation.page.control;

import static org.junit.Assert.*;

import marubinotto.piggydb.model.Tag;
import marubinotto.piggydb.model.entity.RawTag;
import marubinotto.piggydb.presentation.page.control.TagTree;
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
		Tag rootTag = new RawTag("root", 1);
		tagTreeStringShouldBeEqualsStringOfItsOrigin(rootTag);
	}

	@Test
	public void twoLevelTree() throws Exception {
		RawTag techTag = new RawTag("tech", 1);
		RawTag javaTag = new RawTag("java", 2);
		javaTag.getMutableClassification().addTag(techTag);

		RawTag rootTag = new RawTag("root", 3);
		rootTag.getMutableClassification().addTag(javaTag);
		
		tagTreeStringShouldBeEqualsStringOfItsOrigin(rootTag);
	}
	
	@Test
	public void pluralSiblings() throws Exception {
		RawTag rootTag = new RawTag("root", 1);
		rootTag.getMutableClassification().addTag(new RawTag("aaa", 2));
		rootTag.getMutableClassification().addTag(new RawTag("bbb", 3));
		rootTag.getMutableClassification().addTag(new RawTag("ccc", 4));
		
		tagTreeStringShouldBeEqualsStringOfItsOrigin(rootTag);
	}
	
	@Test
	public void pluralSiblingsInReversedOrder() throws Exception {
		RawTag rootTag = new RawTag("root", 1);
		rootTag.getMutableClassification().addTag(new RawTag("ccc", 2));
		rootTag.getMutableClassification().addTag(new RawTag("bbb", 3));
		rootTag.getMutableClassification().addTag(new RawTag("aaa", 4));

		tagTreeStringShouldBeEqualsStringOfItsOrigin(rootTag);
	}
}

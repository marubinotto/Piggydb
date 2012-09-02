package marubinotto.piggydb.ui.page.control;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import marubinotto.piggydb.model.Classifiable;
import marubinotto.piggydb.model.Classification;
import marubinotto.piggydb.model.Tag;
import marubinotto.piggydb.model.auth.User;
import marubinotto.piggydb.ui.page.common.HtmlFragments;
import marubinotto.piggydb.ui.page.common.WebResourcePaths;
import marubinotto.util.Assert;
import marubinotto.util.web.WebUtils;
import net.sf.click.Context;
import net.sf.click.control.Decorator;
import net.sf.click.extras.tree.Tree;
import net.sf.click.extras.tree.TreeNode;
import net.sf.click.util.HtmlStringBuffer;

import org.apache.commons.lang.StringEscapeUtils;

public class TagTree extends Tree {

	private WebResourcePaths webResources;
	private HtmlFragments htmlFragments;

	public TagTree(String name, WebResourcePaths webResources,
			HtmlFragments htmlFragments) {
		super(name);
		setDecorator(createDecorator());
		setAttribute("id", "tree-" + name);
		setAttribute("class", "content-box collapsable-tree");
		this.webResources = webResources;
		this.htmlFragments = htmlFragments;
	}

	@Override
	protected void renderTreeNodeStart(HtmlStringBuffer buffer,
			TreeNode treeNode, int indentation) {

		buffer.elementStart("li");
		buffer.appendAttribute("class", getExpandClass(treeNode));
		buffer.closeTag();

		buffer
				.append("<table class=\"nowrap-frame\" border=\"0\"><tr><td nowrap=\"nowrap\">");

		// Render the node's expand/collapse functionality.
		if (treeNode.hasChildren()) {
			renderExpandAndCollapseAction(buffer, treeNode);
		}
		buffer.append("\n");
	}

	@Override
	@SuppressWarnings({"rawtypes", "unchecked"})
	protected void renderExpandAndCollapseAction(HtmlStringBuffer buffer,
			TreeNode treeNode) {
		buffer.elementStart("a");
		Map hrefParameters = new HashMap(1);
		hrefParameters.put(EXPAND_TREE_NODE_PARAM, treeNode.getId());
		buffer.appendAttribute("href", "#");
		buffer.appendAttribute("class", "toggle");
		buffer.appendAttribute("onclick", "toggleTreeNode(this); return false;");
		buffer.closeTag();

		buffer.append("<img src=\"");
		buffer.append(getContext().getRequest().getContextPath());
		buffer.append("/style/tree/transparent.gif\" border=\"0\" alt=\"\"/>");

		buffer.elementEnd("a");
		buffer.append("\n");
	}

	/**
	 * Override renderTreeNode()
	 */
	private Decorator createDecorator() {
		return new Decorator() {
			public String render(Object object, Context context) {
				TreeNode treeNode = (TreeNode) object;
				HtmlStringBuffer buffer = new HtmlStringBuffer();

				renderIcon(buffer, treeNode);
				buffer.append("&nbsp;");
				buffer.append("</span> ");

				buffer.elementStart("span");
				if (treeNode.isSelected()) {
					buffer.appendAttribute("class", "selected");
				}
				else {
					buffer.appendAttribute("class", "unselected");
				}
				buffer.closeTag();

				renderValue(buffer, treeNode);

				if (treeNode instanceof FirstLevelNode) {
					FirstLevelNode firstLevelNode = (FirstLevelNode)treeNode;
					if (firstLevelNode.removable) {
						buffer.append("&nbsp;");
						buffer.append("<input type=\"image\"");
						buffer.append(" class=\"delete-button\"");
						buffer.append(" name=\"deleteTag\"");
						buffer.append(" src=\"");
						buffer.append(context.getRequest().getContextPath());
						buffer.append("/images/delete.gif\"");
						buffer.append(" onclick=\"piggydb.util.domain.onDeleteTagClick('");
						buffer.append(StringEscapeUtils.escapeHtml((String)treeNode.getValue()));
						buffer.append("', this.form)\"/>");
					}
				}

				buffer.elementEnd("span");

				buffer.append("</td></tr></table>");

				return buffer.toString();
			}
		};
	}

	@Override
	protected void renderIcon(HtmlStringBuffer buffer, TreeNode treeNode) {
		Assert.Property.requireNotNull(this.htmlFragments, "htmlFragments");

		buffer.elementStart("span");
		buffer.appendAttribute("class",
				this.htmlFragments.tagIconClass((String) treeNode.getValue()));
		buffer.append(">");
	}

	@Override
	protected void renderValue(HtmlStringBuffer buffer, TreeNode treeNode) {
		Assert.Property.requireNotNull(this.webResources, "webResources");

		buffer.elementStart("a");
		buffer.appendAttribute("class", "tag");
		buffer.appendAttribute("href",
				this.webResources.tagPath(getTagId(treeNode)));
		buffer.closeTag();
		if (treeNode.getValue() != null) {
			buffer.append(WebUtils.escapeHtml(treeNode.getValue()));
		}
		buffer.elementEnd("a");
		buffer.append("\n");
	}

	@Override
	protected void renderTreeNodeEnd(HtmlStringBuffer buffer, TreeNode treeNode,
			int indentation) {

		buffer.append("</li>\n");
	}

	// Utilities

	public static class FirstLevelNode extends TreeNode {
		public FirstLevelNode(Object value, String id) {
			super(value, id);
		}

		public boolean removable = true;
	}

	/**
	 * Removabilities of first level tags will be the same as the parameter
	 * "removable".
	 */
	public static void restoreTagTree(Tree tagTree,
			Classification classification, boolean removable) {
		Assert.Arg.notNull(tagTree, "tagTree");
		Assert.Arg.notNull(classification, "classification");

		TreeNode rootNode = new TreeNode("root", "root");
		for (Tag tag : classification) {
			FirstLevelNode tagNode = TagTree.buildTagTree(tag);
			tagNode.removable = removable;
			rootNode.add(tagNode);
		}
		tagTree.setRootNode(rootNode);
		tagTree.expandAll();
	}

	/**
	 * Removability of each first level tag will be set respectively via
	 * Classifiable#canRemoveTag.
	 */
	public static void restoreTagTree(Tree tagTree, Classifiable classifiable,
			User user) {
		Assert.Arg.notNull(tagTree, "tagTree");
		Assert.Arg.notNull(classifiable, "classifiable");
		Assert.Arg.notNull(user, "user");

		TreeNode rootNode = new TreeNode("root", "root");
		for (Tag tag : classifiable.getClassification()) {
			FirstLevelNode tagNode = TagTree.buildTagTree(tag);
			tagNode.removable = classifiable.canRemoveTag(tag, user);
			rootNode.add(tagNode);
		}
		tagTree.setRootNode(rootNode);
		tagTree.expandAll();
	}

	public static FirstLevelNode buildTagTree(Tag tag) {
		Assert.Arg.notNull(tag, "tag");
		Assert.Arg.notNull(tag.getId(), "tag.getId()");
		Assert.Arg.notNull(tag.getName(), "tag.getName()");

		FirstLevelNode root = new FirstLevelNode(tag.getName(), generateId(tag));
		buildTagTreeRecursively(tag, root);
		return root;
	}

	private static void buildTagTreeRecursively(Tag tag, TreeNode node) {
		for (Tag parentTag : tag.getClassification()) {
			TreeNode childNode = new TreeNode(parentTag.getName(),
					generateId(parentTag), node);
			buildTagTreeRecursively(parentTag, childNode);
		}
	}

	private final static Random RANDOM = new Random();

	private static String generateId(Tag tag) {
		return tag.getId() + "-" + Long.toString(Math.abs(RANDOM.nextLong()));
	}

	private static Long getTagId(TreeNode node) {
		String nodeId = node.getId();

		int sepIndex = nodeId.indexOf('-');
		if (sepIndex != -1) {
			nodeId = nodeId.substring(0, sepIndex);
		}

		return new Long(nodeId);
	}

	public static String toString(TreeNode treeNode) {
		Assert.Arg.notNull(treeNode, "treeNode");

		StringBuffer buffer = new StringBuffer();
		toStringRecursively(buffer, treeNode);
		return buffer.toString();
	}

	private static void toStringRecursively(StringBuffer buffer, TreeNode treeNode) {
		if (!treeNode.hasChildren()) return;

		if (buffer.length() > 0) buffer.append(" ");
		buffer.append("(");
		boolean first = true;
		for (Object child : treeNode.getChildren()) {
			if (first)
				first = false;
			else
				buffer.append(", ");
			TreeNode childNode = (TreeNode) child;
			buffer.append(childNode.getValue());
			toStringRecursively(buffer, childNode);
		}
		buffer.append(")");
	}
}

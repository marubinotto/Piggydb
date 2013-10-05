package marubinotto.piggydb.ui.page.command;

import static marubinotto.util.web.WebUtils.escapeHtml;

import java.io.PrintWriter;
import java.util.Collection;
import java.util.Set;

import marubinotto.piggydb.model.Tag;
import marubinotto.piggydb.ui.page.common.HtmlFragments;
import marubinotto.util.paging.Page;

public class JsonUtils {
	
	public static final String CONTENT_TYPE = "application/json; charset=UTF-8";
	
	@SuppressWarnings("rawtypes")
	public static void printPageInfo(Page page, PrintWriter out)
	throws Exception {
		out.print("{");
		out.print("\"pageCount\": " + page.getPageCount());
		out.print(", \"pageIndex\": " + page.getPageIndex());
		out.println("}");
	}

	public static void printTags(
		Collection<Tag> tags, 
		Set<Long> hasChildren, 
		PrintWriter out) 
	throws Exception {
		boolean first = true;
		out.println("[");
		for (Tag tag : tags) {
			if (first) first = false; else out.print(",");
			out.print("{");
			out.print("\"id\": " + tag.getId());
			out.print(", \"name\": \"" + escapeHtml(tag.getName()) + "\"");	// TODO add an unescaped name field
			out.print(", \"isTagFragment\": " + tag.isTagFragment());
			out.print(", \"fragmentId\": " + tag.getFragmentId());
			out.print(", \"quickViewableClass\": \"" + HtmlFragments.CLASS_QUICK_VIEWABLE + "\"");
			out.print(", \"hasParents\": " + (tag.getClassification().size() > 0));
			if (hasChildren != null) 
				out.print(", \"hasChildren\": " + hasChildren.contains(tag.getId()));
			if (tag.getAttributes().get("fontSize") != null) 
				out.print(", \"fontSize\": " + tag.getAttributes().get("fontSize"));
			out.println("}");
		}
		out.println("]");
	}
}

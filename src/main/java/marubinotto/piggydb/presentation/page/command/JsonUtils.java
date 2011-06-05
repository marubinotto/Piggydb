package marubinotto.piggydb.presentation.page.command;

import java.io.PrintWriter;
import java.util.Collection;
import java.util.Set;

import marubinotto.piggydb.model.Tag;
import marubinotto.util.paging.Page;
import marubinotto.util.web.WebUtils;

public class JsonUtils {
	
	public static final String CONTENT_TYPE = "application/json; charset=UTF-8";
	
	@SuppressWarnings("unchecked")
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
			out.print(", \"name\": \"" + WebUtils.escapeHtml(tag.getName()) + "\"");	// TODO add an unescaped name field
			out.print(", \"hasParents\": " + (tag.getClassification().size() > 0));
			if (hasChildren != null) 
				out.print(", \"hasChildren\": " + hasChildren.contains(tag.getId()));
			if (tag.getPopularity() != null) 
				out.print(", \"popularity\": " + tag.getPopularity());
			out.println("}");
		}
		out.println("]");
	}
}

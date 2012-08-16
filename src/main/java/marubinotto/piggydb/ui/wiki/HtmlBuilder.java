package marubinotto.piggydb.ui.wiki;

import static marubinotto.util.RegexUtils.compile;
import marubinotto.piggydb.model.Fragment;
import marubinotto.piggydb.model.predicate.Preformatted;
import marubinotto.util.web.WebUtils;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.UnhandledException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.oro.text.regex.MatchResult;
import org.apache.oro.text.regex.Pattern;

public class HtmlBuilder implements DocumentBuilder {

	private static Log logger = LogFactory.getLog(HtmlBuilder.class);

	public void breakBlocks(ParseContext context) {
		closeAllBlocks(context);
	}

	public void startSection(ParseContext context, int level, String title) {
		int headingLevel = level + 1; // NOTE top level is <h2>
		context.print("<h" + headingLevel + ">");
		context.print(title);
		context.println("</h" + headingLevel + ">");
	}

	public void appendToParagraph(ParseContext context, String line) {
		setBlock(context, HtmlBlock.PARAGRAPH, 1);
		context.println(line);
	}

	public void appendToPreformattedText(ParseContext context, String line) {
		setBlock(context, HtmlBlock.PREFORMATTED_TEXT, 1);
		context.println(line);
	}

	public void putHorizontalRule(ParseContext context) {
		closeAllBlocks(context);
		context.println("<hr/>");
	}

	public void appendToBlockquote(ParseContext context, int level, String line) {
		setBlock(context, HtmlBlock.BLOCKQUOTE, level);
		context.println(line);
	}

	public void addUnorderedListElement(ParseContext context, int level, String content) {
		setBlock(context, HtmlBlock.UNORDERED_LIST, level);
		context.print(content);
	}

	public void addOrderedListElement(ParseContext context, int level, String content) {
		setBlock(context, HtmlBlock.ORDERED_LIST, level);
		context.print(content);
	}

	public void addDefinitionListEntry(ParseContext context, String term, String description) {
		setBlock(context, HtmlBlock.DEFINITION_LIST, 1);
		context.print("<dt>");
		context.print(term);
		context.println("</dt>");
		if (description != null && !description.trim().equals("")) {
			context.print("<dd>");
			context.print(description);
			context.println("</dd>");
		}
	}

	public void addTableRow(ParseContext context, String[] values) {
		setBlock(context, HtmlBlock.TABLE, 1);
		context.print("<tr>");
		for (String value : values) {
			context.print("<td>");
			context.print(value);
			context.print("</td>");
		}
		context.println("</tr>");
	}

	public void finish(ParseContext context) {
		closeAllBlocks(context);
	}

	public String escape(String chunk) {
		return WebUtils.escapeHtml(chunk);
	}

	public String appendBreak(String line) {
		return line + "<br/>";
	}

	public String processItalic(ParseContext context, String chunk) {
		return "<i>" + chunk + "</i>";
	}

	public String processBold(ParseContext context, String chunk) {
		return "<b>" + chunk + "</b>";
	}

	public String processDelete(ParseContext context, String chunk) {
		return "<del>" + chunk + "</del>";
	}

	public String processStandardUrl(ParseContext context, String url, boolean preformatted) {
		// Images
		if (!preformatted && context.getMatcher().matches(url, P_IMAGE_URL)) {
			return "<a class=\"img-link\" href=\"" + url + "\"><img src=\"" + url + "\" alt=\"" + url + "\"/></a>";
		}
		// Youtube
		else if (!preformatted && context.getMatcher().contains(url, P_YOUTUBE_URL)) {
			MatchResult matchResult = context.getMatcher().getMatch();
			String youtubeId = matchResult.group(1);
			return makeEmbeddedYoutubeHtml(youtubeId);
		}
		else {
			return "<a href=\"" + url + "\">" + url + "</a>";
		}
	}

	private static final Pattern P_IMAGE_URL = compile("^(http|https):.*\\.(gif|png|jpeg|jpg)$");
	private static final Pattern P_YOUTUBE_URL = compile("youtube\\.com/watch\\?v=([^&\\s]+)");

	private static String makeEmbeddedYoutubeHtml(String id) {
		int width = 560;
		int height = 340;
		String mvUrl = "http://www.youtube.com/v/" + id + "&hl=en&fs=1";

		StringBuilder html = new StringBuilder();
		html.append("<object width=\"" + width + "\" height=\"" + height + "\">");
		html.append("<param name=\"movie\" value=\"" + mvUrl + "\"></param>");
		html.append("<param name=\"allowFullScreen\" value=\"true\"></param>");
		html.append("<param name=\"allowscriptaccess\" value=\"always\"></param>");
		html.append("<embed src=\"" + mvUrl + "\"" + 
			" type=\"application/x-shockwave-flash\"" + 
			" allowscriptaccess=\"always\"" + 
			" allowfullscreen=\"true\"" + 
			" width=\"" + width + "\" height=\"" + height + "\"></embed>");
		html.append("</object>");
		return html.toString();
	}

	public String makeLinkToFragment(ParseContext context, Long fragmentId, String label) {
		String url = context.getWebResources().fragmentPath(fragmentId);
		return "<a href=\"" + url + "\">" + label + "</a>";
	}

	public String makeLinkToFragmentWithDetail(ParseContext context, Fragment fragment) {
		String url = context.getWebResources().fragmentPath(fragment.getId());
		return "<a href=\"" + url + "\">#" + fragment.getId() + "</a> " + 
			(StringUtils.isNotBlank(fragment.getTitle()) ? fragment.getTitle() : "");
	}

	public String makeEmbeddedFragment(ParseContext context, Fragment fragment) {
		logger.debug("Embed: " + fragment.getId() + " FragmentStack: " + context.getFragmentStack());
		if (context.getFragmentStack().contains(fragment.getId())) {
			return null; // avoid a loop
		}
		return makeEmbeddedFragmentRecursively(context, fragment);
	}

	private static String makeEmbeddedFragmentRecursively(ParseContext context, Fragment fragment) {
		if (fragment.isFile()) {
			if (fragment.isImageFile()) {
				return context.getHtmlFragments().fragmentImage(fragment);
			}
			else {
				return context.getHtmlFragments().fileIcon(fragment.getFileType()) + " " + 
					context.getHtmlFragments().linkToFragmentFileWithSize(fragment);
			}
		}
		else if (Preformatted.INSTANCE.evaluate(fragment)) {
			try {
				return context.getHtmlFragments().preformattedContent(
					fragment, context.getWikiParser(), context.getUser());
			}
			catch (Exception e) {
				throw new UnhandledException(e);
			}
		}
		else {
			context.getFragmentStack().push(fragment.getId());
			try {
				return context.getWikiParser().parseNestedly(
					fragment.getContent(), 
					context.getFragmentStack(), 
					context.getUser(), 
					context.getWebResources());
			}
			catch (Exception e) {
				throw new UnhandledException(e);
			}
			finally {
				context.getFragmentStack().pop();
			}
		}
	}

	public String processLabeledLink(ParseContext context, String label, String url) {
		if (context.getMatcher().matches(label, P_IMAGE_URL)) {
			label = "<img src=\"" + label + "\" alt=\"\"/>";
		}
		return "<a href=\"" + url + "\">" + label + "</a>";
	}

	public String processFragmentRef(ParseContext context, String label, long id) {
		String url = context.getWebResources().fragmentPath(id);
		return "<a href=\"" + url + "\">" + label + "</a>";
	}

	public String processTagName(ParseContext context, String tagName) {
		String url = context.getWebResources().tagPathByName(tagName);
		return "<a class=\"tag\" href=\"" + url + "\">" + tagName + "</a>";
	}

	public String processErrorLine(String line) {
		return "<span class=\"error-line\">" + line + "</span>";
	}

	// Internal

	protected HtmlBlock getCurrentBlock(ParseContext context) {
		if (context.getBlockStack().isEmpty()) {
			return null;
		}
		return (HtmlBlock)context.getBlockStack().peek();
	}

	protected int getCurrentBlockLevel(ParseContext context) {
		return context.getBlockStack().size();
	}

	protected void setBlock(ParseContext context, HtmlBlock block, int level) {
		HtmlBlock currentBlock = getCurrentBlock(context);
		if (currentBlock != null && !block.isSameTypeTo(currentBlock)) {
			closeAllBlocks(context);
		}

		int currentBlockLevel = getCurrentBlockLevel(context);
		if (level == currentBlockLevel) {
			block.readyToAppend(context);
		}
		else if (level > currentBlockLevel) {
			int down = level - currentBlockLevel;
			for (int i = 1; i <= down; i++) {
				block.open(context, currentBlockLevel + down);
				context.getBlockStack().push(block);
			}
		}
		else if (level < currentBlockLevel) {
			int up = currentBlockLevel - level;
			for (int i = 0; i < up; i++) {
				closeCurrentBlock(context);
			}
			block.readyToAppend(context);
		}
	}

	protected void closeCurrentBlock(ParseContext context) {
		HtmlBlock currentBlock = (HtmlBlock)context.getBlockStack().pop();
		currentBlock.close(context);
	}

	protected void closeAllBlocks(ParseContext context) {
		while (!context.getBlockStack().isEmpty()) {
			closeCurrentBlock(context);
		}
	}
}

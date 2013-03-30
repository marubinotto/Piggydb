package marubinotto.piggydb.ui.wiki;

import marubinotto.piggydb.model.Fragment;


public interface DocumentBuilder {
	
	//
	// Block
	//

	public void breakBlocks(ParseContext context);
	
	public void startSection(
		ParseContext context,
		int level, 
		String title);
	
	public void appendToParagraph(ParseContext context, String line);
	
	public void appendToPreformattedText(ParseContext context, String line);
	
	public void putHorizontalRule(ParseContext context);
	
	public void appendToBlockquote(
		ParseContext context,
		int level, 
		String line);
	
	public void addUnorderedListElement(
		ParseContext context,
		int level, 
		String content);
	
	public void addOrderedListElement(
		ParseContext context,
		int level, 
		String content);
	
	public void addDefinitionListEntry(
		ParseContext context,
		String term,
		String description);
	
	public void addTableRow(ParseContext context, String[] values);
	
	public void finish(ParseContext context);
	
	
	//
	// Inline
	//
	
	public String escape(String chunk);		// Must avoid to escape wiki format
	
	public String appendBreak(String line);
	
	// styles
	
	public String processSpan(ParseContext context, String chunk, String classes);
	
	public String processItalic(ParseContext context, String chunk);
	
	public String processBold(ParseContext context, String chunk);
	
	public String processDelete(ParseContext context, String chunk);
	
	// links
	
	public String processStandardUrl(ParseContext context, String url, boolean preformatted);
	
	public String makeLinkToFragment(ParseContext context, Long fragmentId, String label);
	
	public String makeLinkToFragmentWithDetail(ParseContext context, Fragment fragment);
	
	public String fragmentUrnToWebUrl(String url, ParseContext context);
	
	// Return null if a loop is detected
	public String makeEmbeddedFragment(ParseContext context, Fragment fragment);
	
	public String processLabeledLink(ParseContext context, String label, String url);
	
	public String processFragmentRef(ParseContext context, String label, long id);
	
	public String processTagName(ParseContext context, String tagName);
	
	
	//
	// Others
	//
	
	public String processErrorLine(String line);
}

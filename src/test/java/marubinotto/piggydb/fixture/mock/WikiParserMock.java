package marubinotto.piggydb.fixture.mock;

import marubinotto.piggydb.ui.wiki.ParseContext;
import marubinotto.piggydb.ui.wiki.WikiParser;

public class WikiParserMock extends WikiParser {
	
	public String arg_wikiText;
	public ParseContext arg_context;
	public String arg_fragmentStack;

	@Override
	public void doParse(String wikiText, ParseContext context) throws Exception {
		this.arg_wikiText = wikiText;
		this.arg_context = context;
		this.arg_fragmentStack = context.getFragmentStack().toString();
		context.print("This was printed at WikiParserMock.doParse");
	}
	
	@Override
	protected void doParsePreformattedText(String preformattedText, ParseContext context)
	throws Exception {
		context.print("WikiParserMock.doParsePreformattedText: " + preformattedText);
	}
}

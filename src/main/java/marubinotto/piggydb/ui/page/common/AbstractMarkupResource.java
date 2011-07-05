package marubinotto.piggydb.ui.page.common;

import marubinotto.piggydb.ui.wiki.WikiParser;

public abstract class AbstractMarkupResource extends AbstractPage {

	public WikiParser wikiParser;
	
	@Override
	protected void setModels() throws Exception {
		super.setModels();
		
		this.wikiParser = (WikiParser)getBean("wikiParser");
	}
}

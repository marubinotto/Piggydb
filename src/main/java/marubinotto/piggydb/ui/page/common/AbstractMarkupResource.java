package marubinotto.piggydb.ui.page.common;

import marubinotto.piggydb.ui.wiki.WikiParser;

public abstract class AbstractMarkupResource extends AbstractPage {
	
	public HtmlFragments html;

	public TemplateUtils utils = TemplateUtils.INSTANCE;
	public WikiParser wikiParser;

	@Override
	public void onInit() {
		super.onInit();
		
		this.html = new HtmlFragments(this.resources);
	}

	@Override
	protected void setModels() throws Exception {
		super.setModels();
			
		this.wikiParser = (WikiParser)getBean("wikiParser");
	}
}

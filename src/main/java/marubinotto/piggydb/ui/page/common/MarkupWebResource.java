package marubinotto.piggydb.ui.page.common;

import org.apache.velocity.tools.generic.LoopTool;

import marubinotto.piggydb.ui.wiki.WikiParser;

public abstract class MarkupWebResource extends WebResource {
	
	public HtmlFragments html;

	public TemplateUtils utils = TemplateUtils.INSTANCE;
	public LoopTool loop;
	public WikiParser wikiParser;

	@Override
	public void onInit() {
		super.onInit();
		
		this.html = new HtmlFragments(this.resources);
	}

	@Override
	protected void setModels() throws Exception {
		super.setModels();
			
		this.loop = new LoopTool();
		this.wikiParser = (WikiParser)getBean("wikiParser");
	}
}

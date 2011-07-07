package marubinotto.piggydb.ui.page.common;

import org.apache.velocity.tools.generic.LoopTool;

import marubinotto.piggydb.model.predicate.Preformatted;
import marubinotto.piggydb.ui.wiki.WikiParser;

public abstract class AbstractTemplateWebResource extends AbstractWebResource {
	
	public HtmlFragments html;

	public TemplateUtils utils = TemplateUtils.INSTANCE;
	public LoopTool loop;
	public WikiParser wikiParser;
	public Preformatted preformatted = Preformatted.INSTANCE;

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

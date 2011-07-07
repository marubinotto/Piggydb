package marubinotto.piggydb.ui.page.common;

import marubinotto.piggydb.model.predicate.Preformatted;
import marubinotto.piggydb.ui.wiki.WikiParser;

import org.apache.velocity.tools.generic.LoopTool;

public abstract class AbstractTemplateWebResource extends AbstractWebResource {
	
	private static final String MK_VERSION = "version";
	private static final String MK_LANG = "lang";
	
	public HtmlFragments html;

	public TemplateUtils utils = TemplateUtils.INSTANCE;
	public LoopTool loop;
	public WikiParser wikiParser;
	public Preformatted preformatted = Preformatted.INSTANCE;
	
	public AbstractTemplateWebResource() {
	}

	@Override
	public void onInit() {
		super.onInit();
		
		this.html = new HtmlFragments(this.resources);
	}

	@Override
	protected void setModels() throws Exception {
		super.setModels();
		
		addModel(MK_VERSION, getWarSetting().getPiggydbVersion());
		addModel(MK_LANG, getContext().getLocale().getLanguage());
			
		this.loop = new LoopTool();
		this.wikiParser = (WikiParser)getBean("wikiParser");
	}
}

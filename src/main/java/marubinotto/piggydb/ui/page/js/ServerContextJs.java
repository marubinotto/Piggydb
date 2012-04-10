package marubinotto.piggydb.ui.page.js;

import marubinotto.piggydb.ui.page.common.AbstractTemplateWebResource;

public class ServerContextJs extends AbstractTemplateWebResource {

	public ServerContextJs() {
	}
	
	@Override
	public String getContentType() {
		return "application/javascript";
	}
	
	@Override
	public void onInit() {
		super.onInit();
		
		// to use the path defined in click.xml as the template for rendering
		setForward((String) null);	
	}
	
	
	public String wikiHelpHref;
	
	@Override
	protected void setModels() throws Exception {
		super.setModels();
		
		this.wikiHelpHref = getMessage("wiki-help-href", 
			getContext().getRequest().getContextPath());
	}
}

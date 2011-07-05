package marubinotto.piggydb.ui.page;

import marubinotto.util.time.DateTime;

public class AboutPage extends AbstractBorderPage {

	@Override
	protected boolean needsAuthentication() {
        return false;
    }
	
	public String cacheControlToken;
	
	@Override 
	protected void setModels() throws Exception {
		super.setModels();
		
		importCssFile("style/piggydb-about.css", true, null);
		this.cacheControlToken = DateTime.getCurrentTime().format("yyyyMMdd");
	}
}

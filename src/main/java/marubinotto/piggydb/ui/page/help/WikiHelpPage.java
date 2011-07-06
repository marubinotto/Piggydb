package marubinotto.piggydb.ui.page.help;

import marubinotto.piggydb.ui.page.common.AbstractWebResource;

public class WikiHelpPage extends AbstractWebResource {

	public String lang;
	public String localizedPath;
	
	@Override 
	public String getPath() {
		if (this.lang == null) {
			return super.getPath();
		}
		
		if (this.localizedPath != null) {
			return this.localizedPath;
		}
		
		this.localizedPath = super.getPath().replaceAll("\\.htm", "-" + lang + ".htm");
		getLogger().debug("this.localizedPath: " + this.localizedPath);
		return this.localizedPath;
	}
	
	@Override 
	public void onRender() {
		disableClientCaching();
	}
}

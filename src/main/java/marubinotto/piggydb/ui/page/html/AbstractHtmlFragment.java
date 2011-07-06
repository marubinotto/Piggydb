package marubinotto.piggydb.ui.page.html;

import marubinotto.piggydb.ui.page.common.AbstractTemplateWebResource;

public abstract class AbstractHtmlFragment extends AbstractTemplateWebResource {
	
	// Disable redirect to return an empty response when a session is expired
	@Override
	public String getRedirect() {
		return null;
	}

	@Override 
	public void onRender() {
		super.onRender();
		disableClientCaching();
	}
}

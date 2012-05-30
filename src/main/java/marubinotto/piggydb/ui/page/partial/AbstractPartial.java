package marubinotto.piggydb.ui.page.partial;

import marubinotto.piggydb.ui.page.LoginPage;
import marubinotto.piggydb.ui.page.common.AbstractMainUiHtml;
import marubinotto.util.message.CodedException;

public abstract class AbstractPartial extends AbstractMainUiHtml {
	
	public String error;
	
	@Override
	public void setRedirect(String location) {
		if (location.startsWith(getContext().getPagePath(LoginPage.class)))
			this.error = getMessage("session-expired");
	}
	
	@Override
	public String getRedirect() {
		// Disable redirect
		return null;
	}

	@Override 
	public void onRender() {
		try {
			super.onRender();
		}
		catch (CodedException e) {
			this.error = getMessage(e);
		}
		catch (Exception e) {
			this.error = e.toString();
			getLogger().error("Unexpected exception", e);
		}
		disableClientCaching();
	}
}

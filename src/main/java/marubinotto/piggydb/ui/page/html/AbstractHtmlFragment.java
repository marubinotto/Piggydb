package marubinotto.piggydb.ui.page.html;

import marubinotto.piggydb.ui.page.common.AbstractMainUiHtml;
import marubinotto.piggydb.ui.page.common.Utils;
import marubinotto.util.CodedException;

public abstract class AbstractHtmlFragment extends AbstractMainUiHtml {
	
	public String error;
	
	// Disable redirect to return an empty response when a session is expired
	@Override
	public String getRedirect() {
		return null;
	}

	@Override 
	public void onRender() {
		try {
			super.onRender();
		}
		catch (CodedException e) {
			this.error = Utils.getMessage(e, this);
		}
		catch (Exception e) {
			this.error = e.toString();
		}
		disableClientCaching();
	}
}

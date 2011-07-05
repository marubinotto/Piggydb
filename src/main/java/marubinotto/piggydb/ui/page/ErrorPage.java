package marubinotto.piggydb.ui.page;

import java.net.SocketException;

import marubinotto.piggydb.ui.page.common.TemplateUtils;
import marubinotto.piggydb.ui.page.common.Utils;
import marubinotto.util.Assert;
import marubinotto.util.CodedException;

import org.apache.commons.lang.exception.ExceptionUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class ErrorPage extends net.sf.click.util.ErrorPage {
	
	private static Log logger = LogFactory.getLog(ErrorPage.class);

	public TemplateUtils utils = TemplateUtils.INSTANCE;

	@Override
	public boolean onSecurityCheck() {
		Throwable error = getError();
		if (error instanceof CodedException) {
			logger.debug(error.toString(), error);
			String message = Utils.getMessage((CodedException)error, this);
			redirectHomeWithMessage(message);
			return false;
		}
		return true;
	}

	@Override
	public void onInit() {
		super.onInit();
		logError();
	}
	
	private void logError()	{
		Throwable error = getError();
		
		if (error== null) return;
		if (ExceptionUtils.getRootCause(error) instanceof SocketException) {
			logger.info(error.toString(), error);
			return;
		}

		logger.error("A system error occurred.", getError());
	}
	
	private void redirectHomeWithMessage(String message) {
    	Assert.Arg.notNull(message, "message");
    	getContext().setFlashAttribute(AbstractPage.SK_MESSAGE, message);
    	setRedirect(HomePage.class);
    }
}

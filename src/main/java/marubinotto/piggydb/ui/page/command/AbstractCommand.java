package marubinotto.piggydb.ui.page.command;

import marubinotto.piggydb.ui.page.common.AbstractWebResource;

import org.apache.commons.lang.UnhandledException;

import com.fasterxml.jackson.databind.ObjectMapper;

public abstract class AbstractCommand extends AbstractWebResource {
  
  protected ObjectMapper JSON = new ObjectMapper();

	@Override 
	public String getPath() {
		return null;
	}

	@Override 
	public void onRender() {
		super.onRender();
		
		disableClientCaching();
		try {
			execute();
		}
		catch (Exception e) {
			throw new UnhandledException(e);
		}
	}
	
	protected abstract void execute() throws Exception;
}

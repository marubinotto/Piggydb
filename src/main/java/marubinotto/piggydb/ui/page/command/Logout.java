package marubinotto.piggydb.ui.page.command;

import marubinotto.piggydb.ui.page.LoginPage;

public class Logout extends AbstractCommand {

	@Override 
	protected void execute() throws Exception {	
		getLogger().info("Processing logout ...");
		getContext().getRequest().getSession().invalidate();
        String path = getContext().getPagePath(LoginPage.class);
        setRedirect(path);
	}
}

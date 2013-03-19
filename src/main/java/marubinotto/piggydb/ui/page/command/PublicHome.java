package marubinotto.piggydb.ui.page.command;

import java.util.List;

import marubinotto.piggydb.model.Fragment;
import marubinotto.piggydb.model.auth.Authentication;
import marubinotto.piggydb.ui.page.DocumentViewPage;

public class PublicHome extends AbstractCommand {
	
	@Override
	protected boolean needsAuthentication() {
		return false;
	}

	@Override 
	protected void execute() throws Exception {
	  Fragment publicHome = getPublicHomeFragment();	
		if (publicHome == null) return;
		
		Long publicHomeId = publicHome.getId();
		getLogger().info("publicHomeId: " + publicHomeId);
		
		DocumentViewPage documentView = (DocumentViewPage) 
			getContext().createPage(DocumentViewPage.class);
		documentView.id = publicHomeId;
		setForward(documentView);
	}
	
	private Fragment getPublicHomeFragment() throws Exception {
	  List<Fragment> fragmentsAtHome = getDomain().getFragmentRepository()
	    .getFragmentsAtHome(Authentication.createAnonymousUser());
	  for (Fragment fragment : fragmentsAtHome) {
	    if (fragment.isPublic()) return fragment;
	  }
	  return null;
	}
}

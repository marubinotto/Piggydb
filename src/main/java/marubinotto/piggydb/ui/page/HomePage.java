package marubinotto.piggydb.ui.page;

import java.util.List;

import marubinotto.piggydb.model.Fragment;
import marubinotto.piggydb.ui.page.common.AbstractFragmentsPage;

public class HomePage extends AbstractFragmentsPage {

	@Override
	protected String getAtomUrl() {
		return getDefaultAtomUrl();
	}

	//
	// Model
	//

	public List<Fragment> homeFragments = EMPTY_FRAGMENTS;
	public Fragment userFragment;

	@Override
	protected void setModels() throws Exception {
		super.setModels();

		importCss("style/piggydb-home.css", true, null);

		setHomeFragments();
		setUserFragment();
		setCommonSidebarModels();
	}

	private void setHomeFragments() throws Exception {
		this.homeFragments = getDomain().getFragmentRepository().getFragmentsAtHome(getUser());
	}

	private void setUserFragment() throws Exception {
		if (getUser().homeFragmentId != null) {
			this.userFragment = getDomain().getFragmentRepository().get(getUser().homeFragmentId);
		}
	}
}

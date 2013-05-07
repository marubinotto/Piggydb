package marubinotto.piggydb.ui.page;

import java.util.List;

import marubinotto.piggydb.model.Fragment;
import marubinotto.piggydb.ui.page.common.AbstractFragmentsPage;
import marubinotto.piggydb.ui.page.common.PageUrl;

public class HomePage extends AbstractFragmentsPage {

	@Override
	protected String getAtomUrl() {
		return getDefaultAtomUrl();
	}

	@Override
	protected PageUrl createThisPageUrl() {
		PageUrl pageUrl = super.createThisPageUrl();
		if (this.date != null) {
			pageUrl.parameters.put(PN_DATE, this.date);
		}
		return pageUrl;
	}

	//
	// Input
	//

	public static final String PN_DATE = "date";
	public String date;

	//
	// Control
	//

	@Override
	public void onInit() {
		super.onInit();
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

	@Override
	public void onRender() {
		super.onRender();
		embedCurrentStateInParameters();
	}

	private void embedCurrentStateInParameters() {
		if (this.date != null) {
			addParameterToCommonForms(PN_DATE, this.date);
		}
	}
}

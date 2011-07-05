package marubinotto.piggydb.ui.page;

import java.util.ArrayList;
import java.util.List;

import marubinotto.piggydb.model.Fragment;
import marubinotto.piggydb.model.ModelUtils;
import marubinotto.piggydb.ui.page.common.AbstractPage;

public class DocumentViewPage extends AbstractPage {

	@Override
	protected boolean needsAuthentication() {
		return false;
	}

	//
	// Input
	//

	public Long id;
	public Fragment fragment;

	@Override
	protected boolean onPreInit() throws Exception {
		if (this.id == null) {
			getLogger().info("Missing parameter: id");
			return true;
		}

		this.fragment = getDomain().getFragmentRepository().get(this.id.longValue());
		if (this.fragment == null) {
			getLogger().info("Missing fragment: #" + this.id);
			return true;
		}

		if (!isAuthenticated() && !this.fragment.isPublic()) {
			getLogger().info("Forbidden: #" + this.id);
			setRedirectToLogin();
			return false;
		}

		fetchTagsAdditionally(this.fragment);

		return true;
	}

	private void fetchTagsAdditionally(Fragment fragment) throws Exception {
		List<Fragment> tagNotFetched = new ArrayList<Fragment>();

		// Grandchildren
		tagNotFetched.addAll(ModelUtils.collectChildrenOfEach(fragment
			.getChildren()));
		// Great-grandchildren
		tagNotFetched.addAll(ModelUtils.collectChildrenOfEach(tagNotFetched));

		getDomain().getFragmentRepository().refreshClassifications(tagNotFetched);
	}

	//
	// Model
	//

	public Boolean publicOnly;

	@Override
	protected void setModels() throws Exception {
		super.setModels();
		this.publicOnly = !isAuthenticated();
	}
}

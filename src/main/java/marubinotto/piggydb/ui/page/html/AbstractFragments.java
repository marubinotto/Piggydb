package marubinotto.piggydb.ui.page.html;

import org.apache.commons.lang.ObjectUtils;

import marubinotto.piggydb.model.Classification;
import marubinotto.piggydb.model.Fragment;
import marubinotto.piggydb.model.FragmentsOptions;
import marubinotto.util.paging.Page;

public abstract class AbstractFragments extends AbstractHtmlFragment {

	//
	// Input
	//

	public String viewId;

	public Integer scale; // 0 - 1000

	public Integer orderBy;
	public Boolean ascending;

	public int pi = 0;

	public static final String SK_SCALE = "fragmentsViewScale";
	public static final String SK_ORDERBY = "fragmentsViewOrderBy";
	public static final String SK_ASCENDING = "fragmentsViewAscending";

	@Override
	public void onInit() {
		super.onInit();

		if (this.scale == null) {
			this.scale = (Integer)ObjectUtils.defaultIfNull(
				getContext().getSessionAttribute(SK_SCALE), 
				getWarSetting().getDefaultFragmentsViewScale());
		}
		if (this.orderBy == null) {
			this.orderBy = (Integer)getContext().getSessionAttribute(SK_ORDERBY);
		}
		if (this.ascending == null) {
			this.ascending = (Boolean)getContext().getSessionAttribute(SK_ASCENDING);
		}
	}

	//
	// Model
	//

	public FragmentsView view;

	public Page<Fragment> fragments;
	public Classification contextTags;

	public boolean firstSet = true;
	public boolean lastSet = false;

	protected FragmentsOptions options = new FragmentsOptions();

	@Override
	protected void setModels() throws Exception {
		super.setModels();

		this.view = new FragmentsView(this.viewId);
		this.view.setScale(this.scale);

		this.options.setPagingOption(this.view.getPageSize(), this.pi);
		this.options.eagerFetching = this.view.needsEagerFetching();
		this.options.setSortOption(this.orderBy, this.ascending);

		setSelectedFragments();
		setFragments();

		if (this.fragments != null) {
			this.firstSet = (this.pi == 0);
			this.lastSet = this.fragments.isLastPage();
		}

		saveStateToSession();
	}

	protected abstract void setFragments() throws Exception;

	private void saveStateToSession() {
		if (this.scale != null) getContext().setSessionAttribute(SK_SCALE, this.scale);
		if (this.orderBy != null) getContext().setSessionAttribute(SK_ORDERBY, this.orderBy);
		if (this.ascending != null) getContext().setSessionAttribute(SK_ASCENDING, this.ascending);
	}
}

package marubinotto.piggydb.ui.page.partial;

import marubinotto.piggydb.ui.page.common.Utils;

public class SelectedFragments extends AbstractFragments {
	
	public final boolean fragmentBatchPage = true;
	
	@Override 
	protected void setSelectedFragments() throws Exception {
		// Do nothing
	}

	@Override 
	protected void setFragments() throws Exception {
		this.fragments = getSession().getSelectedFragments().
			getFragments(
				getDomain().getFragmentRepository(),
				Utils.ALMOST_UNLIMITED_PAGE_SIZE, 
				this.options.pageIndex, 
				this.options.eagerFetching);
	}
}

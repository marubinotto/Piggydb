package marubinotto.piggydb.ui.page.html;

import marubinotto.piggydb.ui.page.ModelFactory;

public class SelectedFragments extends AbstractFragments {
	
	public final boolean fragmentBatchPage = true;
	
	@Override 
	protected void setSelectedFragments() throws Exception {
		// Do nothing
	}

	@Override 
	protected void setFragments() throws Exception {
		this.fragments = getSelectedFragments().getFragments(
			getFragmentRepository(),
			ModelFactory.ALMOST_UNLIMITED_PAGE_SIZE, this.options.pageIndex, 
			this.options.eagerFetching);
	}
}

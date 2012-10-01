package marubinotto.piggydb.ui.page.partial;

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
				ALMOST_UNLIMITED_PAGE_SIZE, 0, 
				this.view.needsEagerFetching());
	}
}

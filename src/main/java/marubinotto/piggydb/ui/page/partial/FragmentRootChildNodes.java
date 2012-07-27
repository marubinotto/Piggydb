package marubinotto.piggydb.ui.page.partial;

public class FragmentRootChildNodes extends AbstractSingleFragment {

	@Override 
	protected boolean fetchesRelations() {
		return true;
	}
	
	@Override 
	protected void setModels() throws Exception {
		super.setModels();
		setSelectedFragments();
	}
}

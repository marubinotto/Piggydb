package marubinotto.piggydb.ui.page.partial;

import java.util.List;

import marubinotto.piggydb.model.FragmentRelation;

public class FragmentChildNodes extends AbstractSingleFragment {

	public Long contextParentId;
	
	public List<FragmentRelation> childRelations;
	
	@Override 
	protected boolean fetchesRelations() {
		return true;
	}
	
	@Override 
	protected void setModels() throws Exception {
		super.setModels();
		
		if (this.fragment != null) {
			this.childRelations = this.fragment.navigateToChildren(this.contextParentId);
		}
		setSelectedFragments();
	}
}

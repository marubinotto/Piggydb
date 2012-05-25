package marubinotto.piggydb.ui.page.partial;

import java.util.List;

import marubinotto.piggydb.model.Fragment;
import marubinotto.piggydb.model.FragmentRelation;

public class FragmentChildNodes extends AbstractPartial {

	public Long id;
	public Long contextParentId;
	
	public Fragment fragment;
	public List<FragmentRelation> childRelations;
	
	@Override 
	protected void setModels() throws Exception {
		super.setModels();
		
		if (this.id == null) return;
		
		this.fragment = getDomain().getFragmentRepository().get(this.id.longValue());		
		this.childRelations = this.fragment.navigateToChildren(this.contextParentId);
			
		setSelectedFragments();
	}
}

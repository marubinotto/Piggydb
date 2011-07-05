package marubinotto.piggydb.ui.page.html;

import marubinotto.piggydb.model.Fragment;

public abstract class AbstractOneFragment extends AbstractHtml {

	public Long id;
	public Fragment fragment;
	
	@Override 
	protected void setModels() throws Exception {
		super.setModels();
		
		if (this.id == null) return;	
		this.fragment = getDomain().getFragmentRepository().get(this.id.longValue());
		setSelectedFragments();
	}
}

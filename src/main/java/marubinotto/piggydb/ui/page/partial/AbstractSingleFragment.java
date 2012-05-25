package marubinotto.piggydb.ui.page.partial;

import marubinotto.piggydb.model.Fragment;

/**
 * HTML Fragment based on a knowledge fragment without its relationships
 */
public abstract class AbstractSingleFragment extends AbstractPartial {

	public Long id;
	public Fragment fragment;
	
	@Override 
	protected void setModels() throws Exception {
		super.setModels();
		
		if (this.id == null) return;
		this.fragment = getDomain().getFragmentRepository().get(this.id.longValue(), false);
	}
}

package marubinotto.piggydb.ui.page.html;

import static marubinotto.util.CollectionUtils.list;

import java.util.List;

import marubinotto.piggydb.model.Fragment;

/**
 * HTML Fragment based on a knowledge fragment without its relationships
 */
public abstract class AbstractOneFragment extends AbstractHtmlFragment {

	public Long id;
	public Fragment fragment;
	
	@Override 
	protected void setModels() throws Exception {
		super.setModels();
		
		if (this.id == null) return;
		List<Fragment> fragments = getDomain().getFragmentRepository()
			.getByIds(list(this.id), null, false);
		this.fragment = fragments.isEmpty() ? null : fragments.get(0);
	}
}

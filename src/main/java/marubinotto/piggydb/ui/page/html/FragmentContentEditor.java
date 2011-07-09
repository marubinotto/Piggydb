package marubinotto.piggydb.ui.page.html;

import static marubinotto.util.CollectionUtils.list;

import java.util.List;

import marubinotto.piggydb.model.Fragment;

public class FragmentContentEditor extends AbstractHtmlFragment {
	
	public Long id;
	public Fragment fragment;

	@Override 
	protected void setModels() throws Exception {
		super.setModels();
		
		if (this.id == null) return;
		
		List<Fragment> fragments = getDomain().getFragmentRepository()
			.getByIds(list(this.id), null, false);
		if (fragments.isEmpty()) return;
		
		this.fragment = fragments.get(0);
	}
}

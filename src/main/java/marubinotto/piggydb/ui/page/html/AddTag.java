package marubinotto.piggydb.ui.page.html;

import marubinotto.piggydb.model.Fragment;
import marubinotto.piggydb.model.Tag;

public class AddTag extends AbstractHtmlFragment {
	
	public Long fragmentId;
	public Long tagId;
	
	public Fragment fragment;

	@Override 
	protected void setModels() throws Exception {
		super.setModels();
		
		if (this.fragmentId == null) return;
		if (this.tagId == null) return;
		
		this.fragment = getDomain().getFragmentRepository().get(this.fragmentId);
		if (this.fragment == null) return;
		
		Tag tag = getDomain().getTagRepository().get(this.tagId);
		if (tag == null) return;
		
		this.fragment.addTagByUser(tag, getUser());
		getDomain().saveFragment(this.fragment, getUser());
	}
}

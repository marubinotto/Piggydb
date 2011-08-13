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
		
		// fragment
		setFragment();
		if (this.fragment == null) return;
		
		// tag
		Tag tag = getTag();
		if (tag == null) return;
		
		// tagging
		this.fragment.addTagByUser(tag, getUser());
		try {
			getDomain().saveFragment(this.fragment, getUser());
		}
		catch (Exception e) {
			setFragment();	// restore the original data
			throw e;
		}
	}
	
	private void setFragment() throws Exception {
		if (this.fragmentId == null) return;
		this.fragment = getDomain().getFragmentRepository().get(this.fragmentId);
	}
	
	private Tag getTag() throws Exception {
		if (this.tagId == null) return null;
		return getDomain().getTagRepository().get(this.tagId);
	}
}

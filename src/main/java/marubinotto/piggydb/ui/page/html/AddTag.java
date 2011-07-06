package marubinotto.piggydb.ui.page.html;

import marubinotto.piggydb.model.Fragment;
import marubinotto.piggydb.model.Tag;
import marubinotto.util.procedure.Procedure;

public class AddTag extends AbstractHtmlFragment {
	
	public Long fragmentId;
	public Long tagId;
	
	public Fragment fragment;

	@Override 
	protected void setModels() throws Exception {
		super.setModels();
		
		getLogger().info("Adding the tag: " + this.tagId + " to the fragment: " + this.fragmentId);
		
		if (this.fragmentId == null) return;
		if (this.tagId == null) return;
		
		this.fragment = getDomain().getFragmentRepository().get(this.fragmentId);
		if (this.fragment == null) return;
		
		Tag tag = getDomain().getTagRepository().get(this.tagId);
		if (tag == null) return;
		
		this.fragment.addTagByUser(tag, getUser());
		getDomain().getTransaction().execute(new Procedure() {
			public Object execute(Object input) throws Exception {
				getDomain().getFragmentRepository().update(fragment);
				return null;
			}
		});
	}
}

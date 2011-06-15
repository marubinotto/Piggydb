package marubinotto.piggydb.ui.page.html;

import marubinotto.piggydb.model.Fragment;
import marubinotto.piggydb.model.Tag;
import marubinotto.util.procedure.Procedure;

public class AddTag extends AbstractHtml {
	
	public Long fragmentId;
	public Long tagId;
	
	public Fragment fragment;

	@Override 
	protected void setModels() throws Exception {
		super.setModels();
		
		getLogger().info("Adding the tag: " + this.tagId + " to the fragment: " + this.fragmentId);
		
		if (this.fragmentId == null) return;
		if (this.tagId == null) return;
		
		this.fragment = getFragmentRepository().get(this.fragmentId);
		if (this.fragment == null) return;
		
		Tag tag = getTagRepository().get(this.tagId);
		if (tag == null) return;
		
		this.fragment.addTagByUser(tag, getUser());
		getTransaction().execute(new Procedure() {
			public Object execute(Object input) throws Exception {
				getFragmentRepository().update(fragment);
				return null;
			}
		});
	}
}

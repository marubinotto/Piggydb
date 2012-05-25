package marubinotto.piggydb.ui.page.partial;

import marubinotto.piggydb.model.Fragment;

public class FragmentEditor extends AbstractSingleFragment {
	
	public String editorId;
	public String editorTitle;

	public int titleMaxLength = Fragment.TITLE_MAX_LENGTH;
	
	@Override 
	protected void setModels() throws Exception {
		super.setModels();
		
		if (this.fragment != null) {
			this.editorId = "fragment-editor-" + this.fragment.getId();
			this.editorTitle = getMessage("edit-fragment");
		}
		else {
			this.editorId = "fragment-editor-new";
			this.editorTitle = getMessage("create-new-fragment");
		}
	}
}

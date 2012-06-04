package marubinotto.piggydb.ui.page.partial;

import marubinotto.piggydb.model.Fragment;
import marubinotto.piggydb.ui.page.control.form.FragmentFormUtils;

public class FragmentEditor extends AbstractSingleFragment {
	
	public String editorId;
	public String editorTitle;

	public int titleMaxLength = Fragment.TITLE_MAX_LENGTH;
	
	public String tags;
	
	@Override 
	protected void setModels() throws Exception {
		super.setModels();
		
		if (this.fragment == null) {
			this.fragment = getDomain().getFragmentRepository().newInstance(getUser());
		}
		
		if (this.fragment.getId() != null) {
			this.editorId = "fragment-editor-" + this.fragment.getId();
			this.editorTitle = getMessage("edit-fragment");
		}
		else {
			this.editorId = "fragment-editor-new";
			this.editorTitle = getMessage("create-new-fragment");
		}
		
		this.tags = FragmentFormUtils.toTagsString(this.fragment.getClassification());
		
		addModel("isMinorEditAvailable", isMinorEditAvailable());
	}
}

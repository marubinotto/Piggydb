package marubinotto.piggydb.ui.page.partial;

public class FragmentEditor extends AbstractFragmentForm {
	
	public String editorId;
	public String editorTitle;
	
	@Override 
	protected void setModels() throws Exception {
		super.setModels();
		
		if (this.fragment.getId() != null) {
			this.editorId = "fragment-editor-" + this.fragment.getId();
			this.editorTitle = 
				getMessage("edit-fragment") + 
				" (#" + this.fragment.getId() + ")";
		}
		else {
			this.editorId = "fragment-editor-new";
			if (this.parent != null) {
				this.editorTitle = getTitlePrefixByParent();
			}
			else {
				this.editorTitle = getMessage("create-new-fragment");
			}
		}
	}
}

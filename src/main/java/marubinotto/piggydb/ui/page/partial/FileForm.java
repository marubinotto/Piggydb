package marubinotto.piggydb.ui.page.partial;

public class FileForm extends AbstractSingleFragment {

	public String title;
	
	@Override 
	protected void setModels() throws Exception {
		super.setModels();
		
		if (this.fragment == null) {
			this.fragment = getDomain().getFragmentRepository().newInstance(getUser());
		}
		
		if (this.fragment.getId() != null) {
			this.title = getMessage("edit-fragment");
		}
		else {
			this.title = getMessage("add-file");
		}
	}
}

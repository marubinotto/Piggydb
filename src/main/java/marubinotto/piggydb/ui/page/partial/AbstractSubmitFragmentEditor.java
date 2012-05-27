package marubinotto.piggydb.ui.page.partial;

public abstract class AbstractSubmitFragmentEditor extends AbstractSingleFragment {

	public String asTag;
	public String title;
	public String tags;
	public String content;
	public String minorEdit;
	
	@Override 
	protected void setModels() throws Exception {
		super.setModels();
		
		if (this.fragment == null) {
			this.fragment = getDomain().getFragmentRepository().newInstance(getUser());
		}
		
		// TODO
	}
}

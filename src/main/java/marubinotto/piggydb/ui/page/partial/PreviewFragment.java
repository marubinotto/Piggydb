package marubinotto.piggydb.ui.page.partial;

public class PreviewFragment extends AbstractSubmitFragmentForm {

	@Override 
	protected void setModels() throws Exception {
		super.setModels();
		
		if (this.fragment == null) {
			this.fragment = getDomain().getFragmentRepository().newInstance(getUser());
		}
		bindValues();
	}
}

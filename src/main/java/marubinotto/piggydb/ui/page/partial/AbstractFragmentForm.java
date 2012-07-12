package marubinotto.piggydb.ui.page.partial;

import marubinotto.piggydb.model.Fragment;
import marubinotto.piggydb.ui.page.control.form.FragmentFormUtils;

public abstract class AbstractFragmentForm extends AbstractSingleFragment {

	public int titleMaxLength = Fragment.TITLE_MAX_LENGTH;
	public String tags;
	
	@Override 
	protected void setModels() throws Exception {
		super.setModels();
		
		if (this.fragment == null) {
			this.fragment = getDomain().getFragmentRepository().newInstance(getUser());
		}
		
		this.tags = FragmentFormUtils.toTagsString(this.fragment.getClassification());
		
		addModel("isMinorEditAvailable", isMinorEditAvailable());
	}
}

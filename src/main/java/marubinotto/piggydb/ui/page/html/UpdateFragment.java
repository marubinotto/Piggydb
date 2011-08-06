package marubinotto.piggydb.ui.page.html;

import static org.apache.commons.lang.StringUtils.trimToNull;
import marubinotto.piggydb.model.Fragment;
import marubinotto.util.procedure.Procedure;

public class UpdateFragment extends AbstractSingleFragment {
	
	public String title;
	public String content;
	public String minorEdit;
	
	private Fragment getFragment() {
		return this.fragment;
	}
	
	private boolean isMinorEdit() {
		return this.minorEdit != null;
	}

	@Override 
	protected void setModels() throws Exception {
		super.setModels();
		
		if (this.fragment == null) return;
		
		this.fragment.setTitleByUser(trimToNull(this.title), getUser());
		this.fragment.setContentByUser(this.content, getUser());
		
		getDomain().getTransaction().execute(new Procedure() {
			public Object execute(Object input) throws Exception {
				getDomain().getFragmentRepository().update(getFragment(), !isMinorEdit());
				return null;
			}
		});
	}
}

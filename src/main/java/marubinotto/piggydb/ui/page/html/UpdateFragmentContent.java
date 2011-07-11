package marubinotto.piggydb.ui.page.html;

import org.apache.commons.lang.UnhandledException;

import marubinotto.piggydb.model.Fragment;
import marubinotto.util.procedure.Procedure;

public class UpdateFragmentContent extends AbstractSingleFragment {
	
	public String content;
	
	private Fragment getFragment() {
		return this.fragment;
	}

	@Override 
	public void onRender() {
		super.onRender();
		
		if (this.fragment == null) return;
		
		this.fragment.setContentByUser(this.content, getUser());
		try {
			getDomain().getTransaction().execute(new Procedure() {
				public Object execute(Object input) throws Exception {
					getDomain().getFragmentRepository().update(getFragment());
					return null;
				}
			});
		}
		catch (Exception e) {
			throw new UnhandledException(e);
		}
	}
}

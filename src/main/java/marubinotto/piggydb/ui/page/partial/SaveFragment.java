package marubinotto.piggydb.ui.page.partial;

import marubinotto.piggydb.model.FragmentRepository;
import marubinotto.util.procedure.Procedure;

public class SaveFragment extends AbstractSubmitFragmentForm {
	
	public String success;
	public Long newId;

	@Override 
	protected void setModels() throws Exception {
		super.setModels();
		
		if (this.fragment == null) {
			this.fragment = getDomain().getFragmentRepository().newInstance(getUser());
		}
		
		bindValues();
		if (hasErrors()) return;
		
		if (this.fragment.getId() == null) {
			this.newId = (Long)getDomain().getTransaction().execute(new Procedure() {
				public Object execute(Object input) throws Exception {
					FragmentRepository repository = getDomain().getFragmentRepository();
					long newId = repository.register(getFragment());
					return newId;
				}
			});
			this.success = getMessage("completed-register-fragment", 
				this.html.linkToFragment(this.newId));
		}
		else {
			// TODO
		}
	}
}

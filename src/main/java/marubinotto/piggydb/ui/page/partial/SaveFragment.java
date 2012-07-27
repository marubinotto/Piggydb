package marubinotto.piggydb.ui.page.partial;

import marubinotto.piggydb.model.FragmentRepository;
import marubinotto.piggydb.model.exception.BaseDataObsoleteException;
import marubinotto.util.procedure.Procedure;

public class SaveFragment extends AbstractSubmitFragmentForm {
	
	public Long parentId;
	
	public String success;
	public Long newId;
	
	public Long getParentId() {
		return this.parentId;
	}

	@Override 
	protected void setModels() throws Exception {
		super.setModels();
		
		if (this.fragment == null) {
			this.fragment = getDomain().getFragmentRepository().newInstance(getUser());
		}
		
		bindValues();	
		if (!hasErrors()) saveFragment();
	}
	
	protected void saveFragment() throws Exception {
		// register
		if (this.fragment.getId() == null) {
			this.newId = (Long)getDomain().getTransaction().execute(new Procedure() {
				public Object execute(Object input) throws Exception {
					FragmentRepository repository = getDomain().getFragmentRepository();
					long newId = repository.register(getFragment());
					if (getParentId() != null) {
						repository.createRelation(getParentId(), newId, getUser());
					}
					return newId;
				}
			});
			this.success = getMessage("completed-register-fragment", 
				this.html.linkToFragment(this.newId), false);
		}
		// update
		else {
			final boolean minorEdit = isMinorEditAvailable() && isMinorEdit();
			try {
				getDomain().getTransaction().execute(new Procedure() {
					public Object execute(Object input) throws Exception {
						FragmentRepository repository = getDomain().getFragmentRepository();
						repository.update(getFragment(), !minorEdit);
						return null;
					}
				});
			}
			catch (BaseDataObsoleteException e) {
				this.error = getMessage("FragmentForm-base-data-obsolete");
			}
		}
	}
}

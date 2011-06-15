package marubinotto.piggydb.ui.page.html;

public class FragmentsByUser extends AbstractFragments {

	public String name;
	
	@Override 
	protected void setFragments() throws Exception {
		if (this.name == null) return;
		
		this.name = modifyIfGarbledByTomcat(this.name);		
		this.fragments = getFragmentRepository().findByUser(this.name, this.options);
	}
}

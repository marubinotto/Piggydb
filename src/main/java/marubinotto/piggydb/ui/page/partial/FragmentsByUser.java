package marubinotto.piggydb.ui.page.partial;

public class FragmentsByUser extends AbstractFragments {

	public String name;
	
	@Override 
	protected void setFragments() throws Exception {
		if (this.name == null) return;
		
		this.name = modifyIfGarbledByTomcat(this.name);
		
		marubinotto.piggydb.model.query.FragmentsByUser query = 
			(marubinotto.piggydb.model.query.FragmentsByUser)getQuery(
				marubinotto.piggydb.model.query.FragmentsByUser.class);
		query.setUserName(this.name);
		this.fragments = getPage(query);
		
		this.label = 
			"<span class=\"miniTagIcon miniTagIcon-user\">&nbsp;</span> " + 
			this.name;
	}
}

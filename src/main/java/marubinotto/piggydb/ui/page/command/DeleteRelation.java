package marubinotto.piggydb.ui.page.command;

import marubinotto.util.procedure.Procedure;

public class DeleteRelation extends Command {
	
	public Long id;

	@Override 
	protected void execute() throws Exception {
		if (this.id == null) return;
		
		final long relationId = this.id;
		getLogger().info("Deleting a relation: " + relationId);
		getDomain().getTransaction().execute(new Procedure() {
			public Object execute(Object input) throws Exception {		
				return getDomain().getFragmentRepository().deleteRelation(relationId, getUser());
			}
		});
	}
}

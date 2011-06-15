package marubinotto.piggydb.ui.page.command;

import marubinotto.util.procedure.Procedure;

public class DeleteRelation extends AbstractCommand {
	
	public Long id;

	@Override 
	protected void execute() throws Exception {
		if (this.id == null) return;
		
		final long relationId = this.id;
		getLogger().info("Deleting a relation: " + relationId);
		getTransaction().execute(new Procedure() {
			public Object execute(Object input) throws Exception {		
				return getFragmentRepository().deleteRelation(relationId, getUser());
			}
		});
	}
}

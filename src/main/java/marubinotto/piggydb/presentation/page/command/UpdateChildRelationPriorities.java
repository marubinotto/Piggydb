package marubinotto.piggydb.presentation.page.command;

import java.util.ArrayList;
import java.util.List;

import marubinotto.piggydb.model.Fragment;
import marubinotto.util.procedure.Procedure;

public class UpdateChildRelationPriorities extends AbstractCommand {
	
	public Long id;

	@Override 
	protected void execute() throws Exception {
		//
		// Parameter: parent
		//
		if (this.id == null) return;
		final Fragment parent = getFragmentRepository().get(this.id);
		if (parent == null) return;
		
		//
		// Parameter: children order
		//
		String[] children = getContext().getRequestParameterValues("child[]");
		if (children == null || children.length == 0) return;
		
		final List<Long> relationOrder = new ArrayList<Long>();
		for (String child : children) relationOrder.add(Long.parseLong(child));
		getLogger().debug("relationOrder: " + relationOrder);

		//
		// Transaction
		//
		try {
			getTransaction().execute(new Procedure() {
				public Object execute(Object input) throws Exception {
					getFragmentRepository().updateChildRelationPriorities(
						parent, relationOrder, getUser());
					return null;
				}
			});
		} 
		catch (Exception e) {
			getContext().getResponse().getWriter().print("error");
		}
	}
}

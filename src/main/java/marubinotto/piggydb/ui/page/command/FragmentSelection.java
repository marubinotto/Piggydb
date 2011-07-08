package marubinotto.piggydb.ui.page.command;

import marubinotto.piggydb.ui.page.model.SelectedFragments;

public class FragmentSelection extends AbstractCommand {
	
	public String command;
	public Long id;

	@Override 
	protected void execute() throws Exception {
		if (this.command == null) return;
		
		SelectedFragments selectedFragments = getSession().getSelectedFragments();
		if ("add".equals(this.command)) {
			if (this.id != null) selectedFragments.add(this.id);
		}
		else if ("remove".equals(this.command)) {
			if (this.id != null) selectedFragments.remove(this.id);
		}
		else if ("clear".equals(this.command)) {
			selectedFragments.clear();
		}
	}
}

package marubinotto.piggydb.ui.page.command;

public class FragmentSelection extends Command {
	
	public String command;
	public Long id;

	@Override 
	protected void execute() throws Exception {
		if (this.command == null) return;
		
		if ("add".equals(this.command)) {
			if (this.id != null) getSelectedFragments().add(this.id);
		}
		else if ("remove".equals(this.command)) {
			if (this.id != null) getSelectedFragments().remove(this.id);
		}
		else if ("clear".equals(this.command)) {
			getSelectedFragments().clear();
		}
	}
}

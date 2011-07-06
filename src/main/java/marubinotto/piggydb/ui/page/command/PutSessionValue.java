package marubinotto.piggydb.ui.page.command;

public class PutSessionValue extends Command {

	public String name;
	public String value;
	
	@Override 
	protected void execute() throws Exception {
		if (this.name == null) return;
		
		getContext().setSessionAttribute(this.name, this.value);
		
		if (getLogger().isDebugEnabled())
			getLogger().debug("Session: " + this.name + " = " + this.value);
	}
}

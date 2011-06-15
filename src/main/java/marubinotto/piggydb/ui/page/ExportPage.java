package marubinotto.piggydb.ui.page;

import marubinotto.piggydb.model.enums.Role;
import marubinotto.piggydb.ui.page.command.ExportDatabase;
import net.sf.click.control.Form;
import net.sf.click.extras.control.PageSubmit;

public class ExportPage extends BorderPage {

    @Override
    protected String[] getAuthorizedRoles() {
    	return new String[]{Role.OWNER.getName()};
    }
    
    
	//
	// Control
	//
    
	public Form exportForm = new Form();
	private PageSubmit exportButton = new PageSubmit("export", ExportDatabase.class);

	@Override
	public void onInit() {
		super.onInit();
		initControls();
	}
	
	private void initControls() {
		this.exportButton.setLabel(getMessage("ExportPage-export"));
		this.exportForm.add(this.exportButton);
	}
}

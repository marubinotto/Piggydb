package marubinotto.piggydb.ui.page.partial;

import marubinotto.piggydb.ui.page.common.PageImports;

public class UploadFile extends AbstractPartial {

	@Override 
	protected void setModels() throws Exception {
		super.setModels();
		
		addModel("jQueryPath", PageImports.JQUERY_PATH);
	}
}

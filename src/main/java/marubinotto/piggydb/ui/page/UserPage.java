package marubinotto.piggydb.ui.page;

import java.io.UnsupportedEncodingException;

import marubinotto.piggydb.model.Fragment;
import marubinotto.piggydb.ui.page.util.PageUrl;

import org.apache.commons.codec.net.URLCodec;
import org.apache.commons.lang.UnhandledException;

public class UserPage extends AbstractFragmentsPage {
	
	@Override
    protected PageUrl createThisPageUrl() {
    	PageUrl pageUrl = super.createThisPageUrl();
    	if (this.name != null) pageUrl.parameters.put(PN_NAME, this.name);
    	return pageUrl;
	}
	
	@Override
	protected String getAtomUrl() {
		if (this.name == null) return null;
    	try {
			return getDefaultAtomUrl() + "?name=" + 
				new URLCodec().encode(this.name, CHAR_ENCODING);
		} 
    	catch (UnsupportedEncodingException e) {
			throw new UnhandledException(e);
		}
    }

	
	//
	// Input
	//
	
	public static final String PN_NAME = "name";
	public String name;
	
	@Override
	public void onInit() {
		super.onInit();
		if (this.name != null) this.name = modifyIfGarbledByTomcat(this.name);
	}
	
	
	//
	// Model
	//

	public Fragment userFragment;
	
	@Override 
	protected void setModels() throws Exception {
		super.setModels();
		
		if (this.name != null) this.htmlTitle  = this.htmlTitle + HTML_TITLE_SEP + this.name;
		
		setUserFragment();
		setCommonSidebarModels();
	}
	
	private void setUserFragment() throws Exception {
		if (this.name == null) return;
		
		Fragment lightFragment = getFragmentRepository().getUserFragment(this.name);
		if (lightFragment != null) {
			this.userFragment = getFragmentRepository().get(lightFragment.getId());
		}
	}
	
	@Override
	public void onRender() {
		super.onRender();
		embedCurrentStateInParameters();
	}
	
	private void embedCurrentStateInParameters() {
		if (this.name != null) addParameterToCommonForms(PN_NAME, this.name);
    }
}

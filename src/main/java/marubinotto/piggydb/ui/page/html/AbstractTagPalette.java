package marubinotto.piggydb.ui.page.html;

public abstract class AbstractTagPalette extends AbstractHtmlFragment {

	public String jsPaletteRef;
	public boolean enableClose = false;
	
	public String viewType;
	
	@Override 
	protected void setModels() throws Exception {
		super.setModels();
		
		this.viewType = getViewType();
	}
	
	protected abstract String getViewType();
}

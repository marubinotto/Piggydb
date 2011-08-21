package marubinotto.piggydb.ui.page;

import marubinotto.piggydb.ui.page.common.AbstractBorderPage;

public class TagsPage extends AbstractBorderPage {

	public long tagCount = 0;;

	@Override 
	protected void setModels() throws Exception {
		super.setModels();
		
		importCssFile("style/piggydb-tags.css", true, null);
		importCssFile("click/tree/tree.css", false, null);		
		importJsFile("scripts/piggydb-tags.js", true);
		
		this.tagCount = getDomain().getTagRepository().size();
	}
}

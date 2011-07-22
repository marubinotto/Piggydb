package marubinotto.piggydb.ui.page.html;

import java.util.List;

import marubinotto.piggydb.model.Tag;
import marubinotto.piggydb.ui.page.common.Utils;

public class TagPaletteFlat extends AbstractTagPalette {
	
	private static final int PAGE_SIZE = Utils.ALMOST_UNLIMITED_PAGE_SIZE;

	public int pi = 0;
	
	public List<Tag> tags;

	@Override 
	protected void setModels() throws Exception {
		super.setModels();
		
		this.tags = getDomain().getTagRepository().orderByName(PAGE_SIZE, this.pi);
	}
}

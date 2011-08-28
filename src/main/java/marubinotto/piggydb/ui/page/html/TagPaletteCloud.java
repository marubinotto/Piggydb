package marubinotto.piggydb.ui.page.html;

import java.util.Set;

import marubinotto.piggydb.model.Tag;
import marubinotto.piggydb.ui.page.model.TagCloud;

public class TagPaletteCloud extends AbstractTagPalette {

	private static final String VIEW_TYPE = "cloud";
	
	public static final int MAX_SIZE = 100;
	public static final int MAX_FONT_SIZE = 32;
	public static final int MIN_FONT_SIZE = 12;
	
	public Set<Tag> tags;
	
	protected String getViewType() {
		return VIEW_TYPE;
	}

	@Override 
	protected void setModels() throws Exception {
		super.setModels();
		
		TagCloud tagCloud = new TagCloud(MAX_SIZE, MAX_FONT_SIZE, MIN_FONT_SIZE);
		tagCloud.setTagRepository(getDomain().getTagRepository());
		this.tags = tagCloud.getTags();
	}
}

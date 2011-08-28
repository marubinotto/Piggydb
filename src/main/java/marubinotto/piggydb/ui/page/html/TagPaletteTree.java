package marubinotto.piggydb.ui.page.html;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import marubinotto.piggydb.model.ModelUtils;
import marubinotto.piggydb.model.Tag;
import marubinotto.piggydb.model.TagRepository;
import marubinotto.piggydb.ui.page.common.Utils;

public class TagPaletteTree extends AbstractTagPalette {
	
	private static final String VIEW_TYPE = "tree";
	
	public Long parent;
	public Long child;
	public boolean enableBack = false;
	
	public List<Tag> tags;
	
	protected String getViewType() {
		return VIEW_TYPE;
	}

	@Override 
	protected void setModels() throws Exception {
		super.setModels();
		
		this.tags = getSiblingTags();
		setHasChildren(this.tags);
	}
	
	private List<Tag> getSiblingTags() throws Exception {
		TagRepository repository = getDomain().getTagRepository();
		if (this.parent != null) {
			// this won't fetch the parent tags of results
			return repository.findByParentTag(
				this.parent, Utils.ALMOST_UNLIMITED_PAGE_SIZE, 0);
		}
		else if (this.child != null) {
			List<Tag> parents = new ArrayList<Tag>();
			Tag childTag = repository.get(this.child);
			if (childTag == null) return parents;
			for (Tag parent : childTag.getClassification()) parents.add(parent);
			return parents;
		}
		else {
			return repository.getRootTags(Utils.ALMOST_UNLIMITED_PAGE_SIZE, 0);
		}
	}
	
	private void setHasChildren(List<Tag> tags) throws Exception {
		Set<Long> hasChildren = getDomain().getTagRepository()
			.selectAllThatHaveChildren(new HashSet<Long>(ModelUtils.toIds(tags)));
		for (Tag tag : tags) {
			if (hasChildren.contains(tag.getId())) {
				tag.getAttributes().put("hasChildren", true);
			}
		}
	}
}

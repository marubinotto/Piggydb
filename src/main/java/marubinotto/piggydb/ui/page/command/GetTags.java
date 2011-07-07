package marubinotto.piggydb.ui.page.command;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.servlet.http.HttpServletResponse;

import marubinotto.piggydb.model.Tag;
import marubinotto.piggydb.ui.page.common.Utils;

public class GetTags extends AbstractCommand {
	
	public Long parent;
	public Long child;

	@Override 
	protected void execute() throws Exception {
		List<Tag> tags = getResult();
		
		HttpServletResponse response = getContext().getResponse();
		response.setContentType(JsonUtils.CONTENT_TYPE);

		Set<Long> hasChildren = selectAllThatHaveChildren(tags);
		JsonUtils.printTags(tags, hasChildren, response.getWriter());
		response.flushBuffer();
	}

	private List<Tag> getResult() throws Exception {
		if (this.parent != null) {
			return getDomain().getTagRepository().findByParentTag(
				this.parent, Utils.ALMOST_UNLIMITED_PAGE_SIZE, 0);
		}
		else if (this.child != null) {
			Tag childTag = getDomain().getTagRepository().get(this.child);
			List<Tag> parents = new ArrayList<Tag>();
			if (childTag == null) return parents;
			for (Tag parent : childTag.getClassification()) parents.add(parent);
			return parents;
		}
		else {
			return getDomain().getTagRepository().getRootTags(Utils.ALMOST_UNLIMITED_PAGE_SIZE, 0);
		}
	}
		
	private Set<Long> selectAllThatHaveChildren(List<Tag> tags) 
	throws Exception {
		Set<Long> tagIds = new HashSet<Long>();
		for (Tag tag : tags) tagIds.add(tag.getId());
		return getDomain().getTagRepository().selectAllThatHaveChildren(tagIds);
	}
}

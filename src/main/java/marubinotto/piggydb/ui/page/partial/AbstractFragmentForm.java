package marubinotto.piggydb.ui.page.partial;

import org.apache.commons.lang.UnhandledException;

import marubinotto.piggydb.model.Filter;
import marubinotto.piggydb.model.Fragment;
import marubinotto.piggydb.model.Tag;
import marubinotto.piggydb.model.exception.AuthorizationException;
import marubinotto.piggydb.model.exception.InvalidTaggingException;
import marubinotto.piggydb.service.FragmentFormUtils;
import marubinotto.piggydb.ui.page.FilterPage;
import marubinotto.util.message.CodedException;

public abstract class AbstractFragmentForm extends AbstractSingleFragment {
	
	public Long parentId;
	public Fragment parent;
	
	public Long tagId;
	public Long filterId;

	public int titleMaxLength = Fragment.TITLE_MAX_LENGTH;
	public String tags;
	
	@Override 
	protected void setModels() throws Exception {
		super.setModels();
		
		if (this.fragment == null) {
			this.fragment = getDomain().getFragmentRepository().newInstance(getUser());
		}
		
		// set default values
		if (this.parentId != null) {
			this.parent = getDomain().getFragmentRepository().get(this.parentId, false);
			if (this.parent == null) {
				throw new CodedException("no-such-fragment", this.parentId.toString());
			}
			for (Tag tag : this.parent.getClassification()) {
				if (isInheritedFromParent(tag)) {
					addDefaultTag(tag);
				}
			}
		}
		if (this.tagId != null) {
			Tag tag = getDomain().getTagRepository().get(this.tagId);
			if (tag == null) {
				throw new CodedException("no-such-tag", this.tagId.toString());
			}
			addDefaultTag(tag);
		}
		if (this.filterId != null) {
			Filter filter = getFilter(this.filterId);
			if (filter != null) {
				for (Tag tag : filter.getIncludes()) addDefaultTag(tag);
			}
		}
		
		this.tags = FragmentFormUtils.toTagsString(this.fragment.getClassification());
		
		addModel("isMinorEditAvailable", isMinorEditAvailable());
	}
	
	private Filter getFilter(Long filterId) throws Exception {
		if (filterId == 0) {
			return (Filter)getContext().getSessionAttribute(FilterPage.SK_NEW_FILTER);
		}
		else {
			return getDomain().getFilterRepository().get(filterId);
		}
	}
	
	private void addDefaultTag(Tag tag) {
		try {
			this.fragment.addTagByUser(tag, getUser());
		}
		catch (InvalidTaggingException e) {
			throw new UnhandledException(e);
		}
		catch (AuthorizationException e) {
			// don't include not-permitted tags
		}
	}
	
	private static boolean isInheritedFromParent(Tag tag) {
		return !tag.getName().startsWith("#");
	}
	
	protected String getTitlePrefixByParent() {
		if (this.parent == null) return "";
		
		String parentName = this.parent.isTag() ? 
			"\"" + this.parent.getTitle() + "\"" : 
			"#" + this.parent.getId() + " " + this.parent.getTitle();
		return parentName + " ⇒ ";
	}
}

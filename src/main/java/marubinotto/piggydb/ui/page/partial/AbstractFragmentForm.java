package marubinotto.piggydb.ui.page.partial;

import org.apache.commons.lang.UnhandledException;

import marubinotto.piggydb.model.Fragment;
import marubinotto.piggydb.model.Tag;
import marubinotto.piggydb.model.exception.AuthorizationException;
import marubinotto.piggydb.model.exception.InvalidTaggingException;
import marubinotto.piggydb.ui.page.control.form.FragmentFormUtils;
import marubinotto.util.message.CodedException;

public abstract class AbstractFragmentForm extends AbstractSingleFragment {
	
	public Long parentId;
	public Fragment parent;

	public int titleMaxLength = Fragment.TITLE_MAX_LENGTH;
	public String tags;
	
	@Override 
	protected void setModels() throws Exception {
		super.setModels();
		
		if (this.fragment == null) {
			this.fragment = getDomain().getFragmentRepository().newInstance(getUser());
		}
		
		if (this.parentId != null) {
			this.parent = getDomain().getFragmentRepository().get(this.parentId, false);
			if (this.parent == null)
				throw new CodedException("no-such-fragment", this.parentId.toString());
			inheritTagsFromParent();
		}
		
		this.tags = FragmentFormUtils.toTagsString(this.fragment.getClassification());
		
		addModel("isMinorEditAvailable", isMinorEditAvailable());
	}
	
	private void inheritTagsFromParent() {
		if (this.parent == null) return;
		
		for (Tag tag : this.parent.getClassification()) {
			if (isInheritedFromParent(tag)) {
				try {
					this.fragment.addTagByUser(tag, getUser());
				}
				catch (InvalidTaggingException e) {
					throw new UnhandledException(e);
				}
				catch (AuthorizationException e) {
					continue;		// don't include not-permitted tags
				}
			}
		}
	}
	
	private static boolean isInheritedFromParent(Tag tag) {
		return !tag.getName().startsWith("#");
	}
	
	protected String getTitlePrefixByParent() {
		if (this.parent == null) return "";
		
		String parentName = this.parent.isTag() ? 
			"&quot;" + this.parent.getTitle() + "&quot;" : 
			"#" + this.parent.getId();
		return parentName + " &rArr; ";
	}
}

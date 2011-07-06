package marubinotto.piggydb.ui.page;

import java.util.ArrayList;
import java.util.List;

import marubinotto.piggydb.model.DuplicateException;
import marubinotto.piggydb.model.Tag;
import marubinotto.piggydb.ui.page.common.BorderPage;
import marubinotto.piggydb.ui.page.common.Utils;
import marubinotto.util.paging.Page;
import marubinotto.util.procedure.Procedure;
import marubinotto.util.web.WebUtils;
import net.sf.click.control.Form;
import net.sf.click.control.Submit;
import net.sf.click.control.TextField;

import org.apache.commons.lang.ObjectUtils;
import org.springframework.core.ErrorCoded;

public class TagsPage extends BorderPage {

	//
	// Commands
	//

	@Override
	protected boolean onPreInit() throws Exception {
		String command = getContext().getRequestParameter("command");
		if (command != null) {
			if (command.equals("classifyAll") && classifyAll()) return false;
			if (command.equals("deleteAll") && deleteAll()) return false;
		}
		return true;
	}
	
	private boolean classifyAll() throws Exception {
		final String[] selectedTagIds = getContext().getRequestParameterValues("selectedTagIds");
		final String[] classifyingTagIds = 
			(String[])ObjectUtils.defaultIfNull(
				getContext().getRequestParameterValues("classifyingTagIds"), 
				new String[0]);
		final String[] classifyingTagNames = 
			(String[])ObjectUtils.defaultIfNull(
				getContext().getRequestParameterValues("classifyingTagNames"), 
				new String[0]);

		if (selectedTagIds == null || selectedTagIds.length == 0) return false;
		
		getDomain().getTransaction().execute(new Procedure() {
			public Object execute(Object input) throws Exception {
				for (String targetTagId : selectedTagIds) {
					Tag targetTag = getDomain().getTagRepository().get(Long.parseLong(targetTagId));
					if (targetTag == null) {
						continue;
					}
					
					for (String classifyingTagId : classifyingTagIds) {
						Tag classifyingTag = getDomain().getTagRepository().get(Long.parseLong(classifyingTagId));
						if (classifyingTag == null) {
							continue;
						}
						attachTag(targetTag, classifyingTag);
					}
					
					for (String classifyingTagName : classifyingTagNames) {
						attachTag(targetTag, classifyingTagName);
					}
					
					getDomain().getTagRepository().update(targetTag);
				}
				return null;
			}
		});
		
		setRedirectWithMessage(TagsPage.class, getMessage());
		return true;
	}
	
	private int taggingCount = 0;
	private List<String> taggingErrors = new ArrayList<String>();
	
	private String getMessage() {
		StringBuilder msg = new StringBuilder();
		msg.append(getMessage("TagsPage-classify-all-completed", this.taggingCount));
		if (this.taggingErrors.size() > 0) {
			msg.append(" <span style=\"color: red; margin-top: 5px;\">(");
			msg.append(this.taggingErrors.size());
			msg.append(" error" + (this.taggingErrors.size() > 1 ? "s" : ""));
			msg.append(")</span>");
		}
		return msg.toString();
	}
	
	private void attachTag(Tag targetTag, Tag classifyingTag) throws Exception {
		getLogger().info("Tagging: " + targetTag.getName() + " <- " + classifyingTag.getName());
		try {
			targetTag.addTagByUser(classifyingTag, getUser());
			this.taggingCount++;
		}
		catch (Exception e) {
			if (e instanceof ErrorCoded)
				addTaggingError(targetTag.getName(), classifyingTag.getName());
			else
				throw e;
		}
	}
	
	private void attachTag(Tag targetTag, String classifyingTagName) 
	throws Exception {
		getLogger().info("Tagging: " + targetTag.getName() + " <- " + classifyingTagName);
		try {
			targetTag.addTagByUser(classifyingTagName, getDomain().getTagRepository(), getUser());
			this.taggingCount++;
		}
		catch (Exception e) {
			if (e instanceof ErrorCoded)
				addTaggingError(targetTag.getName(), classifyingTagName);
			else
				throw e;
		}
	}
	
	private void addTaggingError(String targetTagName, String classifyingTagName) {
		StringBuilder error = new StringBuilder();
		error.append(WebUtils.escapeHtml(targetTagName));
		error.append(" <img src=\"");
		error.append(getContext().getRequest().getContextPath());
		error.append("/images/red-arrow-to-left.gif\" /> ");
		error.append(WebUtils.escapeHtml(classifyingTagName));
		this.taggingErrors.add(error.toString());
	}
	
	private boolean deleteAll() throws Exception {
		final String[] selectedTagIds = getContext().getRequestParameterValues("selectedTagIds");
		
		if (selectedTagIds == null || selectedTagIds.length == 0) return false;
		
		getDomain().getTransaction().execute(new Procedure() {
			public Object execute(Object input) throws Exception {
				for (String tagId : selectedTagIds) {
					getDomain().getTagRepository().delete(Long.parseLong(tagId), getUser());
				}
				return null;
			}
		});
		
		setRedirectWithMessage(TagsPage.class, 
			getMessage("TagsPage-delete-all-completed", selectedTagIds.length));
		return true;
	}
	
	
	//
	// Control
	//
	
	public Form tagForm = new Form();
	private TextField tagNameField = new TextField("tagName", true);

	@Override
	public void onInit() {
		super.onInit();
		initControls();
	}
	
	private void initControls() {
		this.tagNameField.setSize(40);
		this.tagNameField.setMinLength(Tag.MIN_LENGTH);
		this.tagNameField.setMaxLength(Tag.MAX_LENGTH);
		this.tagNameField.setAttribute("class", "watermarked");
		this.tagNameField.setTitle(getMessage("TagsPage-new-tag-name"));
		this.tagForm.add(this.tagNameField);
		this.tagForm.add(new Submit("createTag", getMessage("add"), this, "onCreateTagClick"));
	}

	public boolean onCreateTagClick() throws Exception {
		if (!this.tagForm.isValid()) {
			return true;
		}
		
		String tagName = this.tagNameField.getValue();
		getLogger().info("onCreateTagClick: " + tagName);
		
		// Create a tag with the name
		Tag newTag = null;
		try {
			newTag = getDomain().getTagRepository().newInstance(tagName, getUser());
		} 
		catch (Exception e) {
			Utils.handleFieldError(e, this.tagNameField, this);
			return true;
		}
		
		// Register the tag
		final Tag tagToRegister = newTag;
		long newId;
		try {
			newId = (Long)getDomain().getTransaction().execute(new Procedure() {
				public Object execute(Object input) throws Exception {
					return getDomain().getTagRepository().register(tagToRegister);
				}
			});
		}
		catch (DuplicateException e) {
			this.tagForm.setError(getMessage("tag-name-already-exists"));
			return true;
		}
		getLogger().debug("newId: " + newId);

		setRedirectToThisPage();
		return false;
	}

	
	//
	// Model
	//

	public long tagCount = 0;
	public Page<Tag> recentChanges;
	
	public static final int RECENT_CHANGES_SIZE = 10;

	@Override 
	protected void setModels() throws Exception {
		super.setModels();
		
		importCssFile("style/piggydb-tags.css", true, null);
		importCssFile("click/tree/tree.css", false, null);		
		importJsFile("scripts/piggydb-tags.js", true);
		
		this.tagCount = getDomain().getTagRepository().size();
		this.recentChanges = getDomain().getTagRepository().getRecentChanges(RECENT_CHANGES_SIZE, 0);
	}
}

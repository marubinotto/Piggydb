package marubinotto.piggydb.ui.page;

import static marubinotto.util.message.CodedException.getCodedMessageOrThrow;

import java.util.List;

import marubinotto.piggydb.model.Fragment;
import marubinotto.piggydb.model.RelatedTags;
import marubinotto.piggydb.model.RelatedTags.RelatedTag;
import marubinotto.piggydb.model.Tag;
import marubinotto.piggydb.model.entity.RawFilter;
import marubinotto.piggydb.model.query.FragmentsByFilter;
import marubinotto.piggydb.ui.page.common.AbstractFragmentsPage;
import marubinotto.piggydb.ui.page.common.PageUrl;
import marubinotto.piggydb.ui.page.control.TagTree;
import marubinotto.piggydb.ui.page.control.form.SingleTagForm;
import marubinotto.piggydb.ui.page.model.RecentlyViewed;
import marubinotto.util.Assert;
import marubinotto.util.message.CodedException;
import marubinotto.util.paging.Page;
import marubinotto.util.procedure.Procedure;
import marubinotto.util.web.WebUtils;
import net.sf.click.control.ActionLink;
import net.sf.click.control.Form;
import net.sf.click.control.HiddenField;
import net.sf.click.control.Submit;
import net.sf.click.control.TextField;
import net.sf.click.extras.tree.Tree;

import org.apache.commons.lang.StringUtils;

public class TagPage extends AbstractFragmentsPage {

	@Override
	protected PageUrl createThisPageUrl() {
		PageUrl pageUrl = super.createThisPageUrl();
		if (this.tag != null) pageUrl.parameters.put(PN_TAG_ID, this.tag.getId());
		pageUrl.parameters.put(PN_SUB_TAGS_PAGE_INDEX, this.sbtpi);
		return pageUrl;
	}

	@Override
	protected String getAtomUrl() {
		return getDefaultAtomUrl() + "?id=" + this.tag.getId();
	}

	
	//
	// Input
	//

	public static final String PN_TAG_ID = "id";
	public static final String PN_TAG_NAME = "name";
	public static final String PN_SUB_TAGS_PAGE_INDEX = "sbtpi";

	public Long id;
	public String name;

	public Tag tag;
	public Fragment fragment;
	public boolean edit = false;

	public int sbtpi = 0; // Page index for sub tags
	private int subTagsPageSize = 20;

	public Tag getTag() {
		return this.tag;
	}

	@Override
	protected boolean onPreInit() throws Exception {	
		setTargetTag();
		if (this.tag == null && !getContext().isAjaxRequest()) {
			setRedirect(HomePage.class);
			return false;
		}
		return true;
	}

	public static final String SK_LAST_TAG_ID = TagPage.class.getName() + "#lastTagID";

	private void setTargetTag() throws Exception {
		if (this.id != null) {
			this.tag = getDomain().getTagRepository().get(this.id.longValue());
		}
		else if (this.name != null) {
			this.name = modifyIfGarbledByTomcat(this.name);
			this.name = WebUtils.unescapeHtml(this.name);
			this.tag = getDomain().getTagRepository().getByName(this.name);
		}
		else {
			Long lastId = (Long)getContext().getSessionAttribute(SK_LAST_TAG_ID);
			if (lastId != null) this.tag = getDomain().getTagRepository().get(lastId);
		}
		
		if (this.tag != null) {
			getContext().setSessionAttribute(SK_LAST_TAG_ID, this.tag.getId());
			this.fragment = getDomain().getFragmentRepository().asFragment(this.tag);
		}
	}

	//
	// Control
	//

	@Override
	public void onInit() {
		super.onInit();
		
		initControls();
		if (this.tag != null) applyTargetTagToControls();
	}

	private void initControls() {
		// Target tag
		this.tagNameField.setSize(40);
		this.tagNameForm.add(this.tagNameField);
		this.tagNameForm.add(new Submit("commitRename", "  OK  ", this, "onCommitRenameClick"));

		// Super tags
		this.superTagForm.setListenerForAdd("onAddSuperTagClick");
		this.superTagForm.setListenerForDelete("onRemoveSuperTagClick");
		this.superTagForm.initialize();
		this.superTags = new TagTree("superTags", this.resources, this.html);
		addControl(this.superTags);

		// Sub tags
		this.subTagForm.setListenerForAdd("onAddSubTagClick");
		this.subTagForm.setListenerForDelete("onRemoveSubTagClick");
		this.subTagForm.initialize();

		// Fragments
		this.deleteTrashesForm.setListener(this, "onDeleteTrashes");
	}

	// this.thisPageUrl needs the target model: this.tag
	private void applyTargetTagToControls() {
		Assert.Property.requireNotNull(tag, "tag");

		this.tagNameField.setValue(this.tag.getName());
		
		TagTree.restoreTagTree(this.superTags, this.tag, getUser());
	}
	

	// Target tag

	public Form tagNameForm = new Form();
	private TextField tagNameField = new TextField("tagName", true);
	public ActionLink renameLink = new ActionLink(this, "onRenameClick");
	public ActionLink editLink = new ActionLink(this, "onEditClick");
	public ActionLink deleteLink = new ActionLink(this, "onDeleteClick");
	public boolean renameMode = false;

	public boolean onRenameClick() throws Exception {
		this.renameMode = true;
		return true;
	}

	public boolean onCommitRenameClick() throws Exception {
		Assert.Property.requireNotNull(tag, "tag");

		if (!this.tagNameForm.isValid()) {
			this.renameMode = true;
			return true;
		}

		String newName = this.tagNameField.getValue();
		try {
			this.tag.setNameByUser(newName, getUser());
			getDomain().saveTag(this.tag, getUser());
		}
		catch (Exception e) {
			this.tagNameField.setError(CodedException.getCodedMessageOrThrow(e, this));
			this.renameMode = true;
			return true;
		}

		setRedirectToThisPage();
		return false;
	}
	
	public boolean onEditClick() throws Exception {
		Assert.Property.requireNotNull(tag, "tag");
		
		if (!this.tag.canChange(getUser())) return true;
		
		getDomain().registerFragmentIfNotExists(this.tag, getUser());
		this.fragment = getDomain().getFragmentRepository().asFragment(this.tag);
		this.edit = true;
		return true;
	}

	public boolean onDeleteClick() throws Exception {
		Assert.Property.requireNotNull(tag, "tag");

		Fragment fragment = getDomain().deleteTag(this.tag, getUser());

		String message = getMessage("TagPage-completed-delete-tag", this.tag.getName());
		if (fragment != null) {
			String path = getContext().getPagePath(FragmentPage.class);
			setRedirectWithMessage(path + "?id=" + fragment.getId(), message);
		}
		else {
			setRedirectWithMessage(HomePage.class, message);
		}
		return false;
	}

	
	// Super tags

	public SingleTagForm superTagForm = new SingleTagForm(this);
	private Tree superTags;

	public boolean onAddSuperTagClick() throws Exception {
		Assert.Property.requireNotNull(tag, "tag");

		if (!this.superTagForm.isValid()) {
			return true;
		}

		String tagName = this.superTagForm.tagField.getValue();
		if (StringUtils.isBlank(tagName)) {
			return true;
		}

		getLogger().info("Adding a super-tag: " + tagName + " to: " + this.tag.getName());
		try {
			this.tag.addTagByUser(tagName, getDomain().getTagRepository(), getUser());
		}
		catch (Exception e) {
			this.superTagForm.setError(getCodedMessageOrThrow(e, this));
			return true;
		}
		getDomain().saveTag(this.tag, getUser());

		setRedirectToThisPage();
		return false;
	}

	public boolean onRemoveSuperTagClick() throws Exception {
		Assert.Property.requireNotNull(tag, "tag");

		String tagToRemove = this.superTagForm.tagToDeleteField.getValue();
		if (StringUtils.isBlank(tagToRemove)) {
			return true;
		}

		this.tag.removeTagByUser(tagToRemove, getUser());
		getDomain().saveTag(this.tag, getUser());

		setRedirectToThisPage();
		return false;
	}

	
	// Sub tags

	public SingleTagForm subTagForm = new SingleTagForm(this);

	public boolean onAddSubTagClick() throws Exception {
		Assert.Property.requireNotNull(tag, "tag");

		if (!this.subTagForm.isValid()) {
			return true;
		}

		String tagName = this.subTagForm.tagField.getValue();
		if (StringUtils.isBlank(tagName)) {
			return true;
		}

		getLogger().info("Adding a sub-tag: " + tagName + " to: " + this.tag.getName());
		final Tag subTag = getDomain().getTagRepository().getByName(tagName);
		try {
			if (subTag == null) {
				final Tag newSubTag = getDomain().getTagRepository().newInstance(tagName, getUser());
				newSubTag.addTagByUser(this.tag, getUser());
				getDomain().getTransaction().execute(new Procedure() {
					public Object execute(Object input) throws Exception {
						getDomain().getTagRepository().register(newSubTag);
						return null;
					}
				});
			}
			else {
				subTag.addTagByUser(this.tag, getUser());
				getDomain().saveTag(subTag, getUser());
			}
		}
		catch (Exception e) {
			this.subTagForm.setError(getCodedMessageOrThrow(e, this));
			return true;
		}

		setRedirectToThisPage();
		return false;
	}

	public boolean onRemoveSubTagClick() throws Exception {
		Assert.Property.requireNotNull(tag, "tag");

		String tagToRemove = this.subTagForm.tagToDeleteField.getValue();
		if (StringUtils.isBlank(tagToRemove)) {
			return true;
		}

		Tag subTag = getDomain().getTagRepository().getByName(tagToRemove);
		if (subTag != null && subTag.getClassification().containsTagName(this.tag.getName())) {
			subTag.removeTagByUser(this.tag.getName(), getUser());
			getDomain().saveTag(subTag, getUser());
		}

		setRedirectToThisPage();
		return false;
	}
	
	
	// Fragments

	public Form deleteTrashesForm = new Form();

	public boolean onDeleteTrashes() throws Exception {
		getLogger().info("Deleting trashes ...");

		getDomain().getTransaction().execute(new Procedure() {
			public Object execute(Object input) throws Exception {
				getDomain().getFragmentRepository().deleteTrashes(getUser());
				return null;
			}
		});

		setRedirectToThisPage(getMessage("TagPage-completed-delete-trashes"));
		return false;
	}

	
	//
	// Model
	//

	public Page<Tag> subtags;
	public List<RelatedTag> relatedTags;

	@Override
	protected void setModels() throws Exception {
		super.setModels();
		Assert.Property.requireNotNull(tag, "tag");

		this.htmlTitle = this.htmlTitle + HTML_TITLE_SEP + this.tag.getName();
		
		importCss("style/piggydb-tag.css", true, null);

		this.subtags = getDomain().getTagRepository().
			findByParentTag(this.tag.getId(), this.subTagsPageSize, this.sbtpi);
		setRelatedTags();

		getRecentlyViewed().add(new RecentlyViewed.Entity(RecentlyViewed.TYPE_TAG, this.tag.getId()));

		setCommonSidebarModels();
	}

	private void setRelatedTags() throws Exception {
		RawFilter filter = new RawFilter();
		filter.getClassification().addTag(this.tag);
		
		FragmentsByFilter query = (FragmentsByFilter)
			getDomain().getFragmentRepository().getQuery(FragmentsByFilter.class);
		query.setFilter(filter);
		RelatedTags relatedTags = query.getRelatedTags();
		this.relatedTags = relatedTags.orderByCount(getDomain().getTagRepository());
	}

	@Override
	public void onRender() {
		super.onRender();
		embedCurrentStateInParameters();
	}

	@SuppressWarnings("unchecked")
	private void embedCurrentStateInParameters() {
		Assert.Property.requireNotNull(tag, "tag");

		// For forms
		this.tagNameForm.add(new HiddenField(PN_TAG_ID, this.tag.getId()));
		this.superTagForm.add(new HiddenField(PN_TAG_ID, this.tag.getId()));
		this.subTagForm.add(new HiddenField(PN_TAG_ID, this.tag.getId()));
		this.deleteTrashesForm.add(new HiddenField(PN_TAG_ID, this.tag.getId()));

		addParameterToCommonForms(PN_TAG_ID, this.tag.getId());
		addParameterToCommonForms(PN_SUB_TAGS_PAGE_INDEX, this.sbtpi);

		// For links
		this.renameLink.getParameters().put(PN_TAG_ID, this.tag.getId());
		this.deleteLink.getParameters().put(PN_TAG_ID, this.tag.getId());
	}
}

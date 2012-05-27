package marubinotto.piggydb.ui.page;

import static marubinotto.util.CodedException.getCodedMessageOrThrow;

import java.util.List;
import java.util.Set;

import marubinotto.piggydb.model.Fragment;
import marubinotto.piggydb.model.FragmentRelation;
import marubinotto.piggydb.model.FragmentRepository;
import marubinotto.piggydb.model.ModelUtils;
import marubinotto.piggydb.model.Tag;
import marubinotto.piggydb.model.entity.RawFragment;
import marubinotto.piggydb.model.exception.InvalidTaggingException;
import marubinotto.piggydb.ui.page.common.AbstractFragmentsPage;
import marubinotto.piggydb.ui.page.control.TagTree;
import marubinotto.piggydb.ui.page.control.form.PublicFieldForm;
import marubinotto.piggydb.ui.page.control.form.SingleTagForm;
import marubinotto.piggydb.ui.page.model.SelectedFragments;
import marubinotto.util.paging.Page;
import marubinotto.util.procedure.Procedure;
import net.sf.click.control.Form;
import net.sf.click.control.HiddenField;
import net.sf.click.control.Submit;
import net.sf.click.control.TextField;
import net.sf.click.extras.tree.Tree;

import org.apache.commons.lang.StringUtils;

public class FragmentBatchPage extends AbstractFragmentsPage {

	@Override
	protected boolean showsSelectedFragments() {
		return false;
	}

	//
	// Control
	//

	@Override
	public void onInit() {
		super.onInit();
		initControls();
	}

	private void initControls() {
		// tagForm
		this.tagForm.setListenerForAdd("onAddTagClick");
		this.tagForm.setListenerForDelete("onRemoveCommonTagClick");
		this.tagForm.initialize();
		this.commonTags = new TagTree("commonTags", this.resources, this.html);
		addControl(this.commonTags);

		// parentForm
		this.parentTitleField.setMaxLength(Fragment.TITLE_MAX_LENGTH);
		this.parentTitleField.setAttribute("class", "watermarked");
		this.parentTitleField.setTitle(getMessage("FragmentForm-title"));
		this.parentForm.add(this.parentTitleField);
		this.parentForm.add(new Submit("createParent", getMessage("create"), this, "onCreateParentClick"));
	}

	// Tag form

	public SingleTagForm tagForm = new SingleTagForm(this);
	private Tree commonTags;

	public boolean onAddTagClick() throws Exception {
		if (!this.tagForm.isValid()) return true;

		// tag
		String tagName = this.tagForm.tagField.getValue();
		if (StringUtils.isBlank(tagName)) {
			setRedirectToThisPage();
			return false;
		}	

		// selected fragments
		SelectedFragments selected = getSession().getSelectedFragments();
		if (selected.isEmpty()) {
			setRedirectToThisPage(getMessage("no-selected-fragments"));
			return false;
		}
		final List<Fragment> fragments = 
			selected.getAllFragments(getDomain().getFragmentRepository(), true);

		// do tagging
		try {
			Tag tag = getDomain().getTagRepository().getOrCreateTag(tagName, getUser());
			getDomain().addTagToFragments(fragments, tag, getUser());
		}
		catch (Exception e) {
			this.tagForm.setError(getCodedMessageOrThrow(e, this));
			return true;
		}

		setRedirectToThisPage(
			getMessage(
				"completed-add-tags-to-selected", 
				new Object[]{this.html.linkToTag(tagName)}, 
				false));
		return false;
	}

	public boolean onRemoveCommonTagClick() throws Exception {
		String tagToRemove = this.tagForm.tagToDeleteField.getValue();
		if (StringUtils.isBlank(tagToRemove)) {
			setRedirectToThisPage();
			return false;
		}

		SelectedFragments selected = getSession().getSelectedFragments();
		if (selected.isEmpty()) {
			setRedirectToThisPage(getMessage("no-selected-fragments"));
			return false;
		}
		final List<Fragment> fragments = 
			selected.getAllFragments(getDomain().getFragmentRepository(), true);

		try {
			getDomain().removeTagFromFragments(fragments, tagToRemove, getUser());
		}
		catch (Exception e) {
			this.tagForm.setError(getCodedMessageOrThrow(e, this));
			return true;
		}

		setRedirectToThisPage(
			getMessage(
				"completed-remove-tag", 
				new Object[]{
					this.html.linkToTag(tagToRemove),
					getMessage("FragmentBatchPage-selected-fragments")}, 
				false));
		return false;
	}

	// Parent form

	public Form parentForm = new Form();
	private TextField parentTitleField = new TextField("title", false);

	public boolean onCreateParentClick() throws Exception {
		if (!this.parentForm.isValid()) return true;

		String title = this.parentTitleField.getValue();
		final Fragment parent = getDomain().getFragmentRepository().newInstance(getUser());
		parent.setTitleByUser(title, getUser());

		Long parentId = null;
		try {
			parentId = (Long)getDomain().getTransaction().execute(new Procedure() {
				public Object execute(Object input) throws Exception {
					FragmentRepository repository = getDomain().getFragmentRepository();
					long newId = repository.register(parent);
					for (Long childId : getSession().getSelectedFragments()) {
						repository.createRelation(newId, childId, getUser());
					}
					return newId;
				}
			});
		}
		catch (Exception e) {
			this.parentForm.setError(getCodedMessageOrThrow(e, this));
			return true;
		}

		getSession().setFlashMessage(
			getMessage(
				"completed-register-fragment", 
				this.html.linkToFragment(parentId), 
				false));
		highlightFragment(parentId);
		setRedirect(getContext().getPagePath(FragmentPage.class) + "?id=" + parentId);
		return false;
	}

	// Remove parent form

	public static class RemoveParentForm extends PublicFieldForm {
		public RemoveParentForm(Object listener, String method) {
			super(listener, method);
		}

		public HiddenField parentToRemove = new HiddenField("parentToRemove", Long.class);
	}

	public RemoveParentForm removeParentForm = new RemoveParentForm(this, "onRemoveParentClick");

	public boolean onRemoveParentClick() throws Exception {
		final long parentToRemove = (Long) this.removeParentForm.parentToRemove.getValueObject();

		SelectedFragments selected = getSession().getSelectedFragments();
		if (selected.isEmpty()) return true;
		final List<Fragment> fragments = 
			selected.getAllFragments(getDomain().getFragmentRepository(), true);

		Fragment removed = null;
		try {
			removed = (Fragment)getDomain().getTransaction().execute(new Procedure() {
				public Object execute(Object input) throws Exception {
					Fragment parent = null;
					for (Fragment fragment : fragments) {
						FragmentRelation relation = fragment.getParentRelationByParentId(parentToRemove);
						if (relation != null) {
							getDomain().getFragmentRepository().deleteRelation(relation.getId(), getUser());
							parent = relation.from;
						}
					}
					return parent;
				}
			});
		}
		catch (Exception e) {
			this.parentForm.setError(getCodedMessageOrThrow(e, this));
			return true;
		}

		setRedirectToThisPage(
			getMessage(
				"FragmentBatchPage-completed-remove-parent", 
				new Object[]{
					removed != null ? 
						this.html.fragmentInMessage(removed) : 
						this.html.linkToFragment(parentToRemove)
				}, 
				false));
		return false;
	}

	//
	// Model
	//

	public List<Fragment> commonParents;

	@Override
	protected void setModels() throws Exception {
		super.setModels();

		// always eager fetching in order to get common tags & parents
		Page<Fragment> fragments = getSession().getSelectedFragments().
			getFragments(getDomain().getFragmentRepository(), ALMOST_UNLIMITED_PAGE_SIZE, 0, true);
		setCommonTags(fragments);
		this.commonParents = ModelUtils.getCommonParents(fragments);

		setCommonSidebarModels();
	}

	private void setCommonTags(List<Fragment> fragments) throws InvalidTaggingException {
		Set<Tag> tags = ModelUtils.getCommonTags(fragments);
		RawFragment dummyFragment = new RawFragment();
		for (Tag tag : tags) {
			dummyFragment.getMutableClassification().addTag(tag);
		}
		TagTree.restoreTagTree(this.commonTags, dummyFragment, getUser());
	}
}

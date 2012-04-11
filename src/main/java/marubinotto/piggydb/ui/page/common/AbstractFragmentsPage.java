package marubinotto.piggydb.ui.page.common;

import static marubinotto.util.CollectionUtils.set;
import static org.apache.commons.lang.StringUtils.*;

import java.util.List;
import java.util.Map;

import marubinotto.piggydb.model.Fragment;
import marubinotto.piggydb.model.FragmentRepository;
import marubinotto.piggydb.model.FragmentsOptions.SortOption;
import marubinotto.piggydb.model.ModelUtils;
import marubinotto.piggydb.model.Tag;
import marubinotto.piggydb.model.enums.FragmentField;
import marubinotto.piggydb.model.exception.DuplicateException;
import marubinotto.piggydb.model.exception.NoSuchEntityException;
import marubinotto.piggydb.ui.page.control.FragmentFormPanel;
import marubinotto.piggydb.ui.page.control.form.PublicFieldForm;
import marubinotto.piggydb.ui.page.html.AbstractFragments;
import marubinotto.piggydb.ui.page.model.SelectedFragments;
import marubinotto.util.CodedException;
import marubinotto.util.procedure.Procedure;
import net.sf.click.Context;
import net.sf.click.control.HiddenField;

import org.apache.commons.lang.StringUtils;

public abstract class AbstractFragmentsPage extends AbstractBorderPage {

	public AbstractFragmentsPage() {
	}
	
	protected String getAtomUrl() {
		return null;
	}
	
	protected String getDefaultAtomUrl() {
		return StringUtils.replace(getFullPageUrl(), ".htm", ".atom");
	}


	//
	// Model
	//
	
	public static final String MK_ATOM_URL = "atomUrl";

	public static final String MK_HIGHLIGHTED_FRAGMENT = "highlightedFragment";
	private static final String SK_HIGHLIGHTED_FRAGMENT = "highlightedFragment";

	public Boolean fragmentOperations = true;
	public List<FragmentField> fragmentFields = FragmentField.getEnumList();

	public Integer fragmentsViewScale;
	public Integer fragmentsViewOrderBy;
	public Boolean fragmentsViewAscending;

	@Override
	protected void setModels() throws Exception {
		super.setModels();
		
		String atomUrl = getAtomUrl();
		if (atomUrl != null) addModel(MK_ATOM_URL, atomUrl);

		importCss("style/piggydb-fragments.css", true, null);	
		importBottomJs("js/piggydb-fragments.js", true);
		importBottomJs("js/piggydb.widget.Fragment.js", true);
		importBottomJs("js/piggydb.widget.QuickEdit.js", true);
		importBottomJs("js/piggydb.widget.ContentToggle.js", true);
		importBottomJs("js/piggydb.widget.FragmentTree.js", true);

		if (showsSelectedFragments()) setSelectedFragments();
		setHighlightedFragment();

		this.fragmentsViewScale = (Integer) getContext().getSessionAttribute(
			AbstractFragments.SK_SCALE);
		if (this.fragmentsViewScale == null)
			this.fragmentsViewScale = getWarSetting().getDefaultFragmentsViewScale();

		setFragmentsViewSortOption();
	}

	protected boolean showsSelectedFragments() {
		return true;
	}

	public void highlightFragment(Long id) {
		getContext().setFlashAttribute(SK_HIGHLIGHTED_FRAGMENT, id);
	}

	public static void highlightFragment(Long id, Context context) {
		context.setFlashAttribute(SK_HIGHLIGHTED_FRAGMENT, id);
	}

	private void setHighlightedFragment() {
		Long id = (Long) getContext().getSessionAttribute(SK_HIGHLIGHTED_FRAGMENT);
		if (id != null) addModel(MK_HIGHLIGHTED_FRAGMENT, id);
	}

	private void setFragmentsViewSortOption() {
		SortOption defaultSortOption = SortOption.getDefault();

		this.fragmentsViewOrderBy = (Integer) getContext().getSessionAttribute(
			AbstractFragments.SK_ORDERBY);
		if (this.fragmentsViewOrderBy == null)
			this.fragmentsViewOrderBy = defaultSortOption.orderBy.getValue();

		this.fragmentsViewAscending = (Boolean) getContext().getSessionAttribute(
			AbstractFragments.SK_ASCENDING);
		if (this.fragmentsViewAscending == null)
			this.fragmentsViewAscending = defaultSortOption.ascending;
	}

	//
	// Control
	//

	protected void addParameterToCommonForms(String name, Object value) {
		this.createRelationForm.add(new HiddenField(name, value));
		this.createRelationsToSelectedForm.add(new HiddenField(name, value));
		this.removeTagForm.add(new HiddenField(name, value));
		this.addTagForm.add(new HiddenField(name, value));
		this.addTagsToSelectedForm.add(new HiddenField(name, value));
		this.removeBookmarkForm.add(new HiddenField(name, value));
	}

	// FragmentFormPanel

	protected FragmentFormPanel createFragmentFormPanel() {
		return createFragmentFormPanel(null);
	}

	protected FragmentFormPanel createFragmentFormPanel(String name) {
		FragmentFormPanel panel = (FragmentFormPanel) getBean("fragmentFormPanel");
		panel.setName(name == null ? "fragmentFormPanel" : name);
		panel.setPage(this);
		panel.setUser(getUser());
		addControl(panel);
		return panel;
	}

	// Create a relation

	public static class CreateRelationForm extends PublicFieldForm {
		public CreateRelationForm(Object listener, String method) {
			super(listener, method);
		}

		public HiddenField fromId = new HiddenField("fromId", Long.class);
		public HiddenField toId = new HiddenField("toId", Long.class);
		
		public HiddenField forward = new HiddenField("forward", String.class);
		public HiddenField backward = new HiddenField("backward", String.class);
	}

	public CreateRelationForm createRelationForm = new CreateRelationForm(this,
		"onCreateRelation");

	public final boolean onCreateRelation() throws Exception {
		// [param] fromId, toId
		final Long fromId = (Long)this.createRelationForm.fromId.getValueObject();
		final Long toId = (Long)this.createRelationForm.toId.getValueObject();		
		if (fromId == null || toId == null) {
			setRedirectToThisPage();
			return false;
		}
		if (fromId.equals(toId)) {
			setRedirectToThisPage(getMessage("cannot-relate-to-itself"));
			return false;
		}
		
		// [param] forward, backward
		final String forward = this.createRelationForm.forward.getValue();
		final String backward = this.createRelationForm.backward.getValue();
		if (isBlank(forward) && isBlank(backward)) {
			setRedirectToThisPage();
			return false;
		}

		final FragmentRepository repository = getDomain().getFragmentRepository();
		try {
			getDomain().getTransaction().execute(new Procedure() {
				public Object execute(Object input) throws Exception {
					// forward
					if (isNotBlank(forward))
						repository.createRelation(fromId, toId, getUser());
					// backward
					if (isNotBlank(backward))
						repository.createRelation(toId, fromId, getUser());
					return null;
				}
			});
		}
		catch (CodedException e) {
			setRedirectToThisPage(Utils.getMessage(e, this));
			return false;
		}
		catch (NoSuchEntityException e) {
			setRedirectToThisPage(getMessage("no-such-fragment", e.id));
			return false;
		}

		// Get the node fragments for a result message
		Map<Long, Fragment> fragments = ModelUtils.toIdMap(
			getDomain().getFragmentRepository()
				.getByIds(set(fromId, toId), SortOption.getDefault(), false));
		Fragment from = fragments.get(fromId);
		Fragment to = fragments.get(toId);
		if (from == null || to == null) {
			// [rare case] either of the fragments has been deleted just after created the relation
			setRedirectToThisPage();
			return false;
		}
		
		setRedirectToThisPage(
			getMessage("completed-create-relation", 
			new Object[]{
				this.html.fragmentInMessage(from), 
				this.html.fragmentInMessage(to)},
			false));
		return false;
	}

	// Create relations to the selected fragments

	public static class CreateRelationsToSelectedForm extends PublicFieldForm {
		public CreateRelationsToSelectedForm(Object listener, String method) {
			super(listener, method);
		}

		public HiddenField fromId = new HiddenField("fromId", Long.class);
	}

	public CreateRelationsToSelectedForm createRelationsToSelectedForm = new CreateRelationsToSelectedForm(
		this, "onCreateRelationsToSelected");

	public final boolean onCreateRelationsToSelected() throws Exception {
		// from
		final Long fromId = (Long) this.createRelationsToSelectedForm.fromId
			.getValueObject();
		if (fromId == null) {
			setRedirectToThisPage();
			return false;
		}

		// to
		final SelectedFragments selected = getSession().getSelectedFragments();
		if (selected.isEmpty()) {
			setRedirectToThisPage(getMessage("no-selected-fragments"));
			return false;
		}

		Integer relationCount = (Integer)getDomain().getTransaction().execute(new Procedure() {
			public Object execute(Object input) throws Exception {
				Integer count = 0;
				getLogger().info("Create relations from #" + fromId + " {");
				for (long toId : selected) {
					if (fromId == toId) {
						getLogger().info("  Cannot relate itself");
						continue;
					}
					try {
						getDomain().getFragmentRepository().createRelation(fromId, toId, getUser());
						count++;
						getLogger().info("  → #" + toId);
					}
					catch (NoSuchEntityException e) {
						getLogger().info("  No such fragment: #" + e.id);
						continue;
					}
					catch (DuplicateException e) {
						getLogger().info("  Duplicate relation: #" + toId);
						continue;
					}
				}
				getLogger().info("}");
				return count;
			}
		});

		setRedirectToThisPage(getMessage("completed-create-relations-to-selected",
			new Object[]{relationCount}));
		return false;
	}

	// Remove a tag

	public static class RemoveTagForm extends PublicFieldForm {
		public RemoveTagForm(Object listener, String method) {
			super(listener, method);
		}

		public HiddenField fragmentId = new HiddenField("fragmentId", Long.class);
		public HiddenField tagName = new HiddenField("tagName", String.class);
	}

	public RemoveTagForm removeTagForm = new RemoveTagForm(this, "onRemoveTag");

	public final boolean onRemoveTag() throws Exception {
		long fragmentId = (Long) this.removeTagForm.fragmentId.getValueObject();
		String tagName = this.removeTagForm.tagName.getValue();

		Fragment fragment = getDomain().getFragmentRepository().get(fragmentId);
		if (fragment == null || StringUtils.isBlank(tagName)) {
			setRedirectToThisPage();
			return false;
		}

		getLogger().info("Removing the tag: " + tagName + " from: #" + fragmentId);
		fragment.removeTagByUser(tagName, getUser());
		getDomain().saveFragment(fragment, getUser());

		highlightFragment(fragment.getId());
		setRedirectToThisPage(getMessage("completed-remove-tag", new Object[]{
			this.html.linkToTag(tagName), this.html.fragmentInMessage(fragment)},
			false));
		return false;
	}

	// Add a tag

	public static class AddTagForm extends PublicFieldForm {
		public AddTagForm(Object listener, String method) {
			super(listener, method);
		}

		public HiddenField fragmentId = new HiddenField("fragmentId", Long.class);
		public HiddenField tagName = new HiddenField("tagName", String.class);
	}

	public AddTagForm addTagForm = new AddTagForm(this, "onAddTag");

	public final boolean onAddTag() throws Exception {
		long fragmentId = (Long) this.addTagForm.fragmentId.getValueObject();
		String tagName = this.addTagForm.tagName.getValue();

		Fragment fragment = getDomain().getFragmentRepository().get(fragmentId);
		if (fragment == null) {
			setRedirectToThisPage();
			return false;
		}

		getLogger().info("Add a tag <" + tagName + "> to: #" + fragmentId);
		fragment.addTagByUser(tagName, getDomain().getTagRepository(), getUser());
		getDomain().saveFragment(fragment, getUser());

		String message = null;
		if (tagName.equals(Tag.NAME_TRASH)) {
			message = getMessage(
				"completed-tag-as-trash",
				new Object[]{this.html.linkToFragment(fragmentId),
					this.resources.tagPathByName(Tag.NAME_TRASH)}, false);
		}
		else {
			message = getMessage(
				"completed-add-tag",
				new Object[]{this.html.fragmentInMessage(fragment),
					this.html.linkToTag(tagName)}, false);
		}

		highlightFragment(fragment.getId());
		setRedirectToThisPage(message);
		return false;
	}

	// Add tags to the selected fragments

	public static class AddTagsToSelectedForm extends PublicFieldForm {
		public AddTagsToSelectedForm(Object listener, String method) {
			super(listener, method);
		}

		public HiddenField tagId = new HiddenField("tagId", Long.class);
	}

	public AddTagsToSelectedForm addTagsToSelectedForm = new AddTagsToSelectedForm(
		this, "onAddTagsToSelected");

	public final boolean onAddTagsToSelected() throws Exception {
		// tag
		Long tagId = (Long)this.addTagsToSelectedForm.tagId.getValueObject();
		Tag tag = (tagId != null) ? getDomain().getTagRepository().get(tagId) : null;
		if (tag == null) {
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
			getDomain().addTagToFragments(fragments, tag, getUser());
		}
		catch (Exception e) {
			setRedirectToThisPage(Utils.getCodedMessageOrThrow(e, this));
			return false;
		}

		setRedirectToThisPage(
			getMessage(
				"completed-add-tags-to-selected",
				new Object[]{this.html.linkToTag(tag.getName())}, 
				false));
		return false;
	}

	// Remove from bookmarks

	public static class RemoveBookmarkForm extends PublicFieldForm {
		public RemoveBookmarkForm(Object listener, String method) {
			super(listener, method);
		}

		public HiddenField fragmentId = new HiddenField("fragmentId", Long.class);
	}

	public RemoveBookmarkForm removeBookmarkForm = new RemoveBookmarkForm(this,
		"onRemoveBookmark");

	public final boolean onRemoveBookmark() throws Exception {
		long fragmentId = (Long) this.removeBookmarkForm.fragmentId
			.getValueObject();

		Fragment fragment = getDomain().getFragmentRepository().get(fragmentId);
		if (fragment == null) {
			setRedirectToThisPage();
			return false;
		}

		getLogger().info("Removing #" + fragmentId + " from the bookmarks ...");
		fragment.removeTagsByUserClassifiedAs(Tag.NAME_BOOKMARK, getUser());
		getDomain().saveFragment(fragment, getUser());

		highlightFragment(fragment.getId());
		setRedirectToThisPage(getMessage("completed-remove-bookmark",
			new Object[]{this.html.fragmentInMessage(fragment)}, false));
		return false;
	}
}

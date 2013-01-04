package marubinotto.piggydb.ui.page;

import marubinotto.piggydb.model.Fragment;
import marubinotto.piggydb.model.TagRepository;
import marubinotto.piggydb.ui.page.common.AbstractFragmentsPage;
import marubinotto.piggydb.ui.page.common.PageUrl;
import marubinotto.piggydb.ui.page.control.TagTree;
import marubinotto.piggydb.ui.page.control.form.SingleTagForm;
import marubinotto.piggydb.ui.page.model.RecentlyViewed;
import marubinotto.util.Assert;
import marubinotto.util.message.CodedException;
import net.sf.click.control.HiddenField;
import net.sf.click.extras.tree.Tree;

import org.apache.commons.lang.StringUtils;

public class FragmentPage extends AbstractFragmentsPage {

	@Override
	protected PageUrl createThisPageUrl() {
		PageUrl pageUrl = super.createThisPageUrl();
		if (this.fragment != null) 
			pageUrl.parameters.put(PN_FRAGMENT_ID, this.fragment.getId());
		return pageUrl;
	}

	//
	// Input
	//

	public static final String PN_FRAGMENT_ID = "id";
	public Long id;

	public Fragment fragment;

	public Fragment getFragment() {
		return this.fragment;
	}

	@Override
	protected boolean onPreInit() throws Exception {
		// display home by default
		if (this.id == null) this.id = Fragment.ID_HOME;
		
		this.fragment = getDomain().getFragmentRepository().get(this.id);
		if (this.fragment == null && !getContext().isAjaxRequest()) {
			if (this.id != null)
				setRedirectWithMessage(HomePage.class, getMessage("no-such-fragment", this.id));
			else
				setRedirect(HomePage.class);
			return false;
		}
		
		return true;
	}

	//
	// Control
	//

	@Override
	public void onInit() {
		super.onInit();
		
		initControls();
		if (this.fragment != null) applyTargetFragmentToControls();
	}

	private void initControls() {
		// Super tag
		this.superTagForm.setListenerForAdd("onAddSuperTagClick");
		this.superTagForm.setListenerForDelete("onRemoveSuperTagClick");
		this.superTagForm.initialize();
		this.superTags = new TagTree("superTags", this.resources, this.html);
		addControl(this.superTags);
	}

	// this.thisPageUrl needs the target model: this.fragment
	private void applyTargetFragmentToControls() {
		Assert.Property.requireNotNull(fragment, "fragment");
		
		TagTree.restoreTagTree(this.superTags, this.fragment, getUser());
	}

	

	// Super tag

	public SingleTagForm superTagForm = new SingleTagForm(this);
	private Tree superTags;

	public boolean onAddSuperTagClick() throws Exception {
		Assert.Property.requireNotNull(fragment, "fragment");

		if (!this.superTagForm.isValid()) {
			return true;
		}

		String tagName = this.superTagForm.tagField.getValue();
		if (StringUtils.isBlank(tagName)) {
			return true;
		}

		getLogger().info("Adding a super-tag: " + tagName + " to: " + this.fragment.getId());
		TagRepository tagRepository = getDomain().getTagRepository();
		try {
			this.fragment.addTagByUser(tagName, tagRepository, getUser());
		}
		catch (Exception e) {
			this.superTagForm.setError(CodedException.getCodedMessageOrThrow(e, this));
			return true;
		}
		getDomain().saveFragment(this.fragment, getUser());

		highlightFragment(this.fragment.getId());
		setRedirectToThisPage();
		return false;
	}

	public boolean onRemoveSuperTagClick() throws Exception {
		Assert.Property.requireNotNull(fragment, "fragment");

		String tagToRemove = this.superTagForm.tagToDeleteField.getValue();
		if (StringUtils.isBlank(tagToRemove)) {
			return true;
		}

		this.fragment.removeTagByUser(tagToRemove, getUser());
		getDomain().saveFragment(this.fragment, getUser());

		highlightFragment(this.fragment.getId());
		setRedirectToThisPage(getMessage("completed-remove-tag",
			new Object[]{this.html.linkToTag(tagToRemove), this.html.fragmentInMessage(this.fragment)}, false));
		return false;
	}

	//
	// Model
	//

	@Override
	protected void setModels() throws Exception {
		super.setModels();
		Assert.Property.requireNotNull(fragment, "fragment");

		String fragmentTitle = this.fragment.makeHeadline();
		this.htmlTitle = this.htmlTitle + HTML_TITLE_SEP + (fragmentTitle != null ? fragmentTitle : "");

		getRecentlyViewed().add(
			new RecentlyViewed.Entity(RecentlyViewed.TYPE_FRAGMENT, this.fragment.getId()));

		setCommonSidebarModels();
	}

	@Override
	public void onRender() {
		super.onRender();
		embedCurrentStateInParameters();
	}

	private void embedCurrentStateInParameters() {
		Assert.Property.requireNotNull(fragment, "fragment");

		this.superTagForm.add(new HiddenField(PN_FRAGMENT_ID, this.fragment.getId()));
		addParameterToCommonForms(PN_FRAGMENT_ID, this.fragment.getId());
	}
}

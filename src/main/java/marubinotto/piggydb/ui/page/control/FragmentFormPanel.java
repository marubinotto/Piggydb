package marubinotto.piggydb.ui.page.control;

import java.util.ArrayList;
import java.util.List;

import javax.servlet.ServletContext;

import marubinotto.piggydb.model.AuthorizationException;
import marubinotto.piggydb.model.BaseDataObsoleteException;
import marubinotto.piggydb.model.Classification;
import marubinotto.piggydb.model.Fragment;
import marubinotto.piggydb.model.FragmentRepository;
import marubinotto.piggydb.model.InvalidTaggingException;
import marubinotto.piggydb.model.Tag;
import marubinotto.piggydb.model.TagRepository;
import marubinotto.piggydb.model.User;
import marubinotto.piggydb.ui.page.AbstractBorderPage;
import marubinotto.piggydb.ui.page.AbstractFragmentsPage;
import marubinotto.piggydb.ui.page.control.form.FragmentForm;
import marubinotto.util.Assert;
import marubinotto.util.procedure.Procedure;
import marubinotto.util.procedure.Transaction;
import net.sf.click.control.Panel;
import net.sf.click.util.ClickUtils;

import org.apache.commons.lang.UnhandledException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class FragmentFormPanel extends Panel {
	
	private static Log logger = LogFactory.getLog(FragmentFormPanel.class);
	
	public static final String MK_FORM = "form";
	public static final String MK_TITLE = "formTitle";
	public static final String MK_EDIT_DATA = "editData";
	public static final String MK_BASE_DATA = "baseData";
	public static final String MK_IS_FILE_MODE = "isFileMode";
	public static final String MK_PREVIEW_FLAG = "preview";
	
	// Dependencies
	private Transaction transaction;
	private TagRepository tagRepository;
	private FragmentRepository fragmentRepository;

	// Control
	private AbstractFragmentsPage page;
	private User user;
	private String title;
	private String redirectPathAfterRegistration;
	private boolean active;
	private boolean allowsOnlyOwnerToUploadFile = false;
	private boolean restoresScrollTop = false;
	private boolean disabled = false;
	
	// Model
	private List<Tag> defaultTags = new ArrayList<Tag>();
	private Fragment editData;	
	private Fragment baseData;
	private boolean baseDataObsolete = false;
	private Long parentId;

	// UI 
	public FragmentForm fragmentForm = new FragmentForm();
	
	
	public FragmentFormPanel() {
	}

	@Override
	public String toString() {
		if (this.disabled) return "";
		return super.toString();
	}

	public void setTransaction(Transaction transaction) {
		this.transaction = transaction;
	}

	public void setTagRepository(TagRepository tagRepository) {
		this.tagRepository = tagRepository;
	}
	
	public void setFragmentRepository(FragmentRepository fragmentRepository) {
		this.fragmentRepository = fragmentRepository;
	}
	
	public FragmentRepository getFragmentRepository() {
		return this.fragmentRepository;
	}

	public void setPage(AbstractFragmentsPage page) {
		this.page = page;
	}

	public void setUser(User user) {
		this.user = user;
	}

	public User getUser() {
		return this.user;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public void setRedirectPathAfterRegistration(
		String redirectPathAfterRegistration) {
		this.redirectPathAfterRegistration = redirectPathAfterRegistration;
	}

	public boolean isActive() {
		return this.active;
	}
	
	public void setActive(boolean active) {
		this.active = active;
	}

	public void setAllowsOnlyOwnerToUploadFile(boolean allowsOnlyOwnerToUploadFile) {
		this.allowsOnlyOwnerToUploadFile = allowsOnlyOwnerToUploadFile;
	}
	
	private boolean allowsToUploadFile() {
		return !this.allowsOnlyOwnerToUploadFile || (this.user != null && this.user.isOwner());
	}

	public void setRestoresScrollTop(boolean restoresScrollTop) {
		this.restoresScrollTop = restoresScrollTop;
	}

	public void addDefaultTag(Tag tag) {
		Assert.Arg.notNull(tag, "tag");
		this.defaultTags.add(tag);
	}
	
	public void addDefaultTags(Classification classification) {
		Assert.Arg.notNull(classification, "classification");
		for (Tag tag : classification) addDefaultTag(tag);
	}

	public void setBaseData(Fragment baseData) {
		this.baseData = baseData;
	}

	public Fragment getEditData() {
		return this.editData;
	}

	public void setParentId(Long parentId) {
		this.parentId = parentId;
	}

	public Long getParentId() {
		return this.parentId;
	}
	
	
	//
	// Control
	//
	
	@Override
	public void onDeploy(ServletContext servletContext) {
		String[] files = new String[]{
			"/marubinotto/piggydb/ui/page/control/twistie-down.gif",
			"/marubinotto/piggydb/ui/page/control/twistie-up.gif",
			"/marubinotto/piggydb/ui/page/control/delete.gif"};
		ClickUtils.deployFiles(servletContext, files, "images");
	}

	@Override
	public void onInit() {
		super.onInit();
		
		initControls();
		
		this.editData = this.fragmentForm.restoreEditData();
		if (this.editData == null) this.editData = createEditData();
		if (this.editData == null) this.disabled = true;

		// as default values of the form when no data has been posted.
		// if data has been posted, this values will be overwritten.
		if (this.editData != null) this.fragmentForm.fillForm(this.editData, getUser());
	}

	private void initControls() {
		Assert.Property.requireNotNull(tagRepository, "tagRepository");
		
		if (this.name == null) {
			this.name = "fragmentFormPanel";
		}		
		if (this.title == null) {
			this.title = getMessage("FragmentFormPanel-create-new-fragment");
		}
		
		this.fragmentForm.initialize(this.name + "-form", this, this.page);
		if (this.baseData != null) {
			if (this.baseData.isFile()) this.fragmentForm.setFileMode();
			if (getUser().isOwner() || 
				getUser().getName().equals(this.baseData.getLastUpdaterOrCreator())) {
				this.fragmentForm.enableMinorEdit();
			}
			this.baseDataObsolete = this.fragmentForm.checkIfBaseDataUpdatedByAnother(this.baseData);
		}
		if (!allowsToUploadFile()) {
			this.fragmentForm.disableUploadFile();
		}
		addControl(this.fragmentForm);
		addModel(MK_FORM, this.fragmentForm);
	}
	
	/**
	 * Return null if the user is not authorized to edit the fragment.
	 */
	private Fragment createEditData() {
		// Existing fragment (update)
		if (this.baseData != null) {
			if (!this.baseData.canChange(this.user)) return null;
			return this.baseData.copyForUpdate();		
		}
		// New fragment (create)
		else {
			Fragment newFragment = null;
			try {
				newFragment = getFragmentRepository().newInstance(this.user);
			} 
			catch (AuthorizationException e) {
				return null;
			}
			for (Tag tag : this.defaultTags) {
				try {
					newFragment.addTagByUser(tag, this.user);
				}
				catch (InvalidTaggingException e) {
					throw new UnhandledException(e);
				}
				catch (AuthorizationException e) {
					continue;		// don't include not-permitted tags
				}
			}
			return newFragment;
		}
	}
	
	public boolean onPreviewClick() throws Exception {
		if (this.editData == null) {
			this.page.setRedirect(this.redirectPathAfterRegistration);
			return false;
		}
		
		// Panel settings for preview
		this.active = true;
		restoreScrollTop();
		this.fragmentForm.restoreContentFieldHeight();
		
		// Validate the input
		if (!this.fragmentForm.isValid()) return true;
		if (!this.fragmentForm.bindFormValuesTo(this.editData, getUser(), this.tagRepository)) 
			return true;
		
		this.fragmentForm.createEditSessionIfNotYet(this.editData);
		addModel(MK_PREVIEW_FLAG, "true");	
		return true;
	}
	
	public boolean onRegisterClick() throws Exception {
		Assert.Property.requireNotNull(transaction, "transaction");
		Assert.Property.requireNotNull(fragmentRepository, "fragmentRepository");
		Assert.Property.requireNotNull(page, "page");
		Assert.Property.requireNotNull(redirectPathAfterRegistration, "redirectPathAfterRegistration");

		if (this.editData == null) {
			this.page.setRedirect(this.redirectPathAfterRegistration);
			return false;
		}
		
		if (!this.fragmentForm.isValid() || this.baseDataObsolete) {
			restoreScrollTop();
			return true;
		}
		if (!this.fragmentForm.bindFormValuesTo(this.editData, getUser(), this.tagRepository)) {
			restoreScrollTop();
			return true;
		}
		
		if (this.baseData != null) {
			final boolean minorEdit = this.fragmentForm.isMinorEditChecked();
			try {
				this.transaction.execute(new Procedure() {
					public Object execute(Object input) throws Exception {
						if (minorEdit) {
							getFragmentRepository().update(getEditData(), false);
						}
						else {
							getFragmentRepository().update(getEditData());
						}
						return null;
					}
				});
			}
			catch (BaseDataObsoleteException e) {
				this.fragmentForm.setError(getMessage("FragmentForm-base-data-obsolete"));
				this.fragmentForm.disableRegistration();
				restoreScrollTop();
				return true;
			}
			this.page.highlightFragment(getEditData().getId());
		}
		else {
			logger.info("Registering a fragment ...");
			Long fragmentId = (Long)this.transaction.execute(new Procedure() {
				public Object execute(Object input) throws Exception {
					FragmentRepository repository = getFragmentRepository();
					long newId = repository.register(getEditData());
					if (getParentId() != null) {
						repository.createRelation(getParentId(), newId, getUser());
					}
					return newId;
				}
			});
			this.page.setFlashMessage(
				getMessage(
					"completed-register-fragment", 
					this.page.html.linkToFragment(fragmentId)));
			this.page.highlightFragment(fragmentId);
		}
		
		this.fragmentForm.closeEditSession();
		this.page.setRedirect(this.redirectPathAfterRegistration);
		return false;
	}
	
	public boolean onCancelClick() throws Exception {
		this.fragmentForm.closeEditSession();
		this.page.setRedirect(this.redirectPathAfterRegistration);
		return false;
	}
	
	private void restoreScrollTop() {
		if (this.restoresScrollTop) {
			getContext().setFlashAttribute(AbstractBorderPage.SK_SCROLL_TOP_ELEMENT, getName());
		}
	}
	
	
	//
	// Model
	//

	@Override
	public void onRender() {
		super.onRender();
		
		addModel(MK_TITLE, this.title);
		addModel(MK_IS_FILE_MODE, this.fragmentForm.isFileMode());
		if (this.baseData != null) addModel(MK_BASE_DATA, this.baseData);
		if (this.editData != null) addModel(MK_EDIT_DATA, this.editData); // for preview

		if (!this.fragmentForm.isValid()) {
			this.active = true;
		}
	}
}

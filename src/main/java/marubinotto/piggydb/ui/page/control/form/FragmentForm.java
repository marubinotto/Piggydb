package marubinotto.piggydb.ui.page.control.form;

import java.util.ArrayList;
import java.util.List;

import marubinotto.piggydb.model.Classification;
import marubinotto.piggydb.model.Fragment;
import marubinotto.piggydb.model.Tag;
import marubinotto.piggydb.model.TagRepository;
import marubinotto.piggydb.model.User;
import marubinotto.piggydb.model.exception.DuplicateException;
import marubinotto.piggydb.model.exception.InvalidTagNameException;
import marubinotto.piggydb.model.exception.InvalidTaggingException;
import marubinotto.piggydb.model.exception.InvalidTitleException;
import marubinotto.piggydb.ui.page.common.Utils;
import marubinotto.piggydb.ui.page.control.EditDataStore;
import marubinotto.piggydb.ui.page.control.FragmentContentField;
import marubinotto.util.Assert;
import marubinotto.util.time.DateTime;
import marubinotto.util.web.WebMessageSource;
import net.sf.click.control.Checkbox;
import net.sf.click.control.FileField;
import net.sf.click.control.Form;
import net.sf.click.control.HiddenField;
import net.sf.click.control.Radio;
import net.sf.click.control.RadioGroup;
import net.sf.click.control.Submit;
import net.sf.click.control.TextField;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class FragmentForm extends Form {
	
	private static Log logger = LogFactory.getLog(FragmentForm.class);
	
	private WebMessageSource messages;
	
	private HiddenField editSessionIdField = new HiddenField();
	private HiddenField baseTimestampField = new HiddenField("baseTimestamp", String.class);
	
	private TextField titleField = new TextField("title", false);
	private Checkbox asTagCheckbox = new Checkbox("asTag", false);

	public static final String CONTENT_TYPE_TEXT = "text";
	public static final String CONTENT_TYPE_FILE = "file";
	
	// contentTypeField is the final value of the content type
	// When registration, this value will set via JavaScript when the switch is changed
	// When update, this value will set via base data
	private HiddenField contentTypeField = new HiddenField("contentType", CONTENT_TYPE_TEXT);
	private RadioGroup contentTypeSwitch = new RadioGroup("contentTypeSwitch", false);
	
	private FragmentContentField contentField = new FragmentContentField("content", false);
	private HiddenField contentFieldHeightField = new HiddenField("contentFieldHeight", Integer.class);
	
	private FileField fileField = new FileField("file", false);
	
	private TextField tagsField = new TextField("tags");
	
	private Submit previewSubmit;
	private Submit registerSubmit;
	private Checkbox minorEditCheckbox = new Checkbox("minorEdit", false);

	public FragmentForm() {
	}

	public void initialize(String name, Object listener, WebMessageSource messages) {
		Assert.Arg.notNull(name, "name");
		Assert.Arg.notNull(listener, "listener");
		Assert.Arg.notNull(messages, "messages");

		setName(name);
		this.messages = messages;
		
		this.editSessionIdField.setName(this.name + "-editSessionId");
		this.editSessionIdField.setValueClass(String.class);
		add(this.editSessionIdField);
		add(this.baseTimestampField);	

		this.titleField.setLabel(messages.getMessage("FragmentForm-title"));
		this.titleField.setSize(60);
		this.titleField.setMaxLength(Fragment.TITLE_MAX_LENGTH);
		this.titleField.setAttribute("class", "fragment-title watermarked");
		this.titleField.setTitle(messages.getMessage("FragmentForm-title"));
		add(this.titleField);
		
		this.asTagCheckbox.setAttribute("class", "fragment-as-tag");
		add(this.asTagCheckbox);

		add(this.contentTypeField);
		this.contentTypeSwitch.setVerticalLayout(false);
		this.contentTypeSwitch.add(
			new Radio(CONTENT_TYPE_TEXT, messages.getMessage("FragmentForm-text")));
		this.contentTypeSwitch.add(
			new Radio(CONTENT_TYPE_FILE, messages.getMessage("FragmentForm-file")));
		this.contentTypeSwitch.setValue(CONTENT_TYPE_TEXT);		// default
		add(this.contentTypeSwitch);
		
		this.contentField.setLabel(messages.getMessage("FragmentForm-content"));
		this.contentField.setTitle(messages.getMessage("FragmentForm-content"));
		add(this.contentField);
		add(this.contentFieldHeightField);
		
		this.fileField.setSize(70);
		add(this.fileField);
		
		this.tagsField.setLabel(messages.getMessage("FragmentForm-tags"));
		this.tagsField.setSize(60);
		this.tagsField.setAttribute("class", "fragment-tags watermarked");
		this.tagsField.setTitle(messages.getMessage("FragmentForm-tags"));
		add(this.tagsField);

		this.previewSubmit = new Submit("preview", messages.getMessage("preview"), listener, "onPreviewClick");
		this.registerSubmit = new Submit("register", messages.getMessage("register"), listener, "onRegisterClick");
		add(this.previewSubmit);
		add(this.registerSubmit);
		add(new Submit("cancel", messages.getMessage("cancel"), listener, "onCancelClick"));
	}
	
	public void fillForm(Fragment fragment, User user) {
		Assert.Arg.notNull(fragment, "fragment");
		Assert.Arg.notNull(user, "user");
		
		this.titleField.setValue(fragment.getTitle());
		this.asTagCheckbox.setChecked(fragment.isTag());
		this.contentField.setValue(fragment.getContent());
		if (!fragment.getClassification().isEmpty()) {
			this.tagsField.setValue(toString(fragment.getClassification()));
		}
		
		if (!fragment.canChangeTitle(user)) {
			this.titleField.setDisabled(true);
		}
	}
	
	public boolean isFileMode() {
		return CONTENT_TYPE_FILE.equals(this.contentTypeField.getValue());
	}
	
	public void setFileMode() {
		this.contentTypeField.setValue(CONTENT_TYPE_FILE);
		this.contentTypeSwitch.setValue(CONTENT_TYPE_FILE);
	}
	
	public boolean isMinorEditChecked() {
		return this.minorEditCheckbox.isChecked();
	}
	
	public boolean isUpdateMode() {
		return StringUtils.isNotBlank(this.baseTimestampField.getValue());
	}
	
	public void enableMinorEdit() {
		this.minorEditCheckbox.setLabel(this.messages.getMessage("FragmentForm-minor-edit"));
		add(this.minorEditCheckbox);
	}
	
	public void disableUploadFile() {
		this.contentTypeSwitch.setDisabled(true);
		this.fileField.setDisabled(true);
		if (isFileMode()) this.registerSubmit.setDisabled(true);
	}
	
	public void disableRegistration() {
		this.previewSubmit.setDisabled(true);
		this.registerSubmit.setDisabled(true);
		this.minorEditCheckbox.setDisabled(true);
	}
	
	public static final char TAGS_SEPARATOR = ',';
	
	public static List<String> splitIntoTagNames(String tags) {
		String[] rawEntries = StringUtils.split(tags, TAGS_SEPARATOR);
		List<String> tagNames = new ArrayList<String>();
		for (String entry : rawEntries) {
			if (StringUtils.isBlank(entry)) continue;
			tagNames.add(entry.trim());
		}
		return tagNames;
	}
	
	public String toString(Classification classification) {
		StringBuilder string = new StringBuilder();
		for (Tag tag : classification) {
			if (string.length() > 0) string.append(TAGS_SEPARATOR + " ");
			string.append(tag.getName());
		}
		return string.toString();
	}
	
	private static final String TIMESTAMP_FORMAT = "yyyyMMddHHmmssS";
	
	public boolean checkIfBaseDataUpdatedByAnother(Fragment baseData) {
		Assert.Arg.notNull(baseData, "baseData");
		
		String baseTimestamp = getContext().getRequestParameter(this.baseTimestampField.getName());
		String timestamp = baseData.getUpdateDatetime().format(TIMESTAMP_FORMAT);
		if (StringUtils.isNotBlank(baseTimestamp)) {
			if (!timestamp.equals(baseTimestamp)) {
				setError(this.messages.getMessage("FragmentForm-base-data-obsolete"));
				disableRegistration();
				return true;
			}
		}
		else {
			this.baseTimestampField.setValue(timestamp);
		}
		return false;
	}
	
	public void restoreContentFieldHeight() {
		Integer height = (Integer)this.contentFieldHeightField.getValueObject();
		if (height != null) this.contentField.setStyle("height", height.toString() + "px");
	}
	
	
	//
	// Object binding
	//
	
	public boolean bindFormValuesTo(
		Fragment object, 
		User user, 
		TagRepository tagRepository) 
	throws Exception {
		// title
		try {
			object.setTitleByUser(
				emptyToNull(this.titleField.getValue()), user);
		} 
		catch (Exception e) {
			Utils.handleFieldError(e, this.titleField, getPage());
			return false;
		}
		
		// as a tag
		try {
			object.setAsTagByUser(this.asTagCheckbox.isChecked(), user);
		}
		catch (Exception e) {
			Utils.handleFieldError(e, this.asTagCheckbox, getPage());
			return false;
		}
		
		// content
		if (isFileMode()) {
			if (!isUpdateMode()) {
				if (!validateFileInput()) return false;
			}
			if (isFileItemValidFile(this.fileField.getFileItem())) {
				object.setFileInput(this.fileField.getFileItem());
			}
		}
		else {
			try {
				object.setContentByUser(
					emptyToNull(this.contentField.getValue()), user);
			} 
			catch (Exception e) {
				Utils.handleFieldError(e, this.contentField, getPage());
				return false;
			}
		}
		
		// tags
		List<String> tagNames = splitIntoTagNames(this.tagsField.getValue());
		try {
			object.updateTagsByUser(tagNames, tagRepository, user);
		} 
		catch (Exception e) {
			Utils.handleFieldError(e, this.tagsField, getPage());
			return false;
		}
		
		try {
			object.validateTagRole(user, tagRepository);
		}
		catch (Exception e) {
			if (e instanceof InvalidTitleException ||
					e instanceof InvalidTagNameException ||
					e instanceof DuplicateException) {
				Utils.handleFieldError(e, this.titleField, getPage());
			}
			else if (e instanceof InvalidTaggingException) {
				Utils.handleFieldError(e, this.tagsField, getPage());
			}
			else {
				Utils.handleFormError(e, this, getPage());
			}
			return false;
		}
		
		return true;
	}
	
	private String emptyToNull(String value) {
		if (value == null || value.equals("")) return null;
		return value;
	}
	
	private boolean validateFileInput() {
		if (!isFileItemValidFile(this.fileField.getFileItem())) {
			this.fileField.setError(
				getMessage(
					"file-required-error", 
					new Object[]{
						this.messages.getMessage("FragmentForm-content")
					}));
			return false;
		}
		return true;
	}
	
	private static boolean isFileItemValidFile(FileItem fileItem) {
		return fileItem != null && 
			!fileItem.isFormField() &&
			StringUtils.isNotBlank(fileItem.getName()) && 
			fileItem.getSize() > 0;
	}

	
	//
	// Edit Session
	//

	public String getEditSessionId() {
		return getContext().getRequestParameter(this.editSessionIdField.getName());
	}
	
	public void createEditSessionIfNotYet(Fragment editData) {
		if (StringUtils.isNotBlank(getEditSessionId())) return;

		String newSessionId = DateTime.getCurrentTime().format(TIMESTAMP_FORMAT);
		EditDataStore store = EditDataStore.getStore(getContext());
		store.data.put(newSessionId, editData);
		this.editSessionIdField.setValue(newSessionId);

		logger.info("Create an edit session: " + newSessionId + " / " + store.data.size());
	}

	public Fragment restoreEditData() {
		String sessionId = getEditSessionId();
		if (StringUtils.isBlank(sessionId)) return null;
		
		return EditDataStore.getStore(getContext()).data.get(sessionId);
	}
	
	public void closeEditSession() {
		String sessionId = getEditSessionId();
		if (StringUtils.isBlank(sessionId)) return;
		
		EditDataStore store = EditDataStore.getStore(getContext());
		store.data.remove(sessionId);
		
		logger.info("Close an edit session: " + sessionId + " / " + store.data.size());
	}
}

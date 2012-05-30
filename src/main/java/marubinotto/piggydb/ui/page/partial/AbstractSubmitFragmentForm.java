package marubinotto.piggydb.ui.page.partial;

import static org.apache.commons.lang.StringUtils.trimToNull;
import static marubinotto.util.message.CodedException.getCodedMessageOrThrow;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import marubinotto.piggydb.model.exception.DuplicateException;
import marubinotto.piggydb.model.exception.InvalidTagNameException;
import marubinotto.piggydb.model.exception.InvalidTaggingException;
import marubinotto.piggydb.model.exception.InvalidTitleException;
import marubinotto.piggydb.ui.page.control.form.FragmentFormUtils;
import marubinotto.util.Assert;

public abstract class AbstractSubmitFragmentForm extends AbstractSingleFragment {

	public String asTag;
	public String title;
	public String tags;
	public String content;
	public String minorEdit;
	
	public Map<String, String> fieldErrors = new HashMap<String, String>();
	
	public boolean asTag() {
		return this.asTag != null;
	}

	public boolean isMinorEdit() {
		return this.minorEdit != null;
	}
	
	public boolean hasErrors() {
		if (this.error != null) return true;
		if (!this.fieldErrors.isEmpty()) return true;
		return false;
	}
	
	public static String emptyToNull(String value) {
		if (value == null) return null;
		return value.equals("") ? null : value;
	}
	
	public void bindValues() throws Exception {
		Assert.Property.requireNotNull(this.fragment, "fragment");
		
		// as a tag
		try {
			this.fragment.setAsTagByUser(asTag(), getUser());
		}
		catch (Exception e) {
			this.fieldErrors.put("asTag", getCodedMessageOrThrow(e, this));
		}
		
		// title
		try {
			this.fragment.setTitleByUser(trimToNull(this.title), getUser());
		} 
		catch (Exception e) {
			this.fieldErrors.put("title", getCodedMessageOrThrow(e, this));
		}
		
		// tags
		List<String> tagNames = FragmentFormUtils.splitTagsString(this.tags);
		try {
			this.fragment.updateTagsByUser(tagNames, getDomain().getTagRepository(), getUser());
		} 
		catch (Exception e) {
			this.fieldErrors.put("tags", getCodedMessageOrThrow(e, this));
		}
		
		// content
		try {
			this.fragment.setContentByUser(emptyToNull(this.content), getUser());
		} 
		catch (Exception e) {
			this.fieldErrors.put("content", getCodedMessageOrThrow(e, this));
		}
		
		if (hasErrors()) return;
		
		try {
			this.fragment.validateAsTag(getUser(), getDomain().getTagRepository());
		}
		catch (Exception e) {
			if (e instanceof InvalidTitleException ||
					e instanceof InvalidTagNameException ||
					e instanceof DuplicateException) {
				this.fieldErrors.put("title", getCodedMessageOrThrow(e, this));
			}
			else if (e instanceof InvalidTaggingException) {
				this.fieldErrors.put("tags", getCodedMessageOrThrow(e, this));
			}
			else {
				this.error = getCodedMessageOrThrow(e, this);
			}
		}
	}
}

package marubinotto.piggydb.ui.page.control.form;

import java.util.ArrayList;
import java.util.List;

import marubinotto.piggydb.model.Classification;
import marubinotto.piggydb.model.Tag;

import org.apache.commons.lang.StringUtils;

public class FragmentFormUtils {

	public static final char TAGS_SEPARATOR = ',';
	
	public static List<String> splitTagsString(String tags) {
		if (tags == null) return new ArrayList<String>();
		
		String[] rawEntries = StringUtils.split(tags, TAGS_SEPARATOR);
		List<String> tagNames = new ArrayList<String>();
		for (String entry : rawEntries) {
			if (StringUtils.isBlank(entry)) continue;
			tagNames.add(entry.trim());
		}
		return tagNames;
	}
	
	public static String toTagsString(Classification classification) {
		StringBuilder string = new StringBuilder();
		for (Tag tag : classification) {
			if (string.length() > 0) string.append(TAGS_SEPARATOR + " ");
			string.append(tag.getName());
		}
		return string.toString();
	}
}

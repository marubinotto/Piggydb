package marubinotto.piggydb.model.predicate;

import static marubinotto.util.CollectionUtils.set;

import java.util.Set;

import marubinotto.piggydb.model.Classifiable;
import marubinotto.util.Assert;

public class Preformatted extends PredicateBase {
	
	public static final Preformatted INSTANCE = new Preformatted();
	
	public static final String TAG_NAME = "#pre";
	public static final String CODE_TAG_NAME = "#code";
		
	public static final String LANG_TAG_PREFIX = "#lang-";
	public static final Set<String> LANG_NAMES = set(
		"bsh", "c", "cc", "cpp", "cs", "csh", "cyc", "cv", "htm", "html",
	    "java", "js", "m", "mxml", "perl", "pl", "pm", "py", "rb", "sh",
	    "xhtml", "xml", "xsl");

	@Override
	protected boolean evaluate(Classifiable classifiable) {
		return classifiable.getClassification().isSubordinateOf(TAG_NAME);
	}
	
	public boolean isCode(Classifiable classifiable) {
		Assert.Arg.notNull(classifiable, "classifiable");
		return classifiable.getClassification().isSubordinateOf(CODE_TAG_NAME);
	}
	
	public String getLanguageName(Classifiable classifiable) {
		Assert.Arg.notNull(classifiable, "classifiable");
		for (String lang : LANG_NAMES) {
			String tagName = LANG_TAG_PREFIX + lang;
			if (classifiable.getClassification().isSubordinateOf(tagName)) {
				return lang;
			}
		}
		return null;
	}
}

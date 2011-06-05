package marubinotto.piggydb.util;

import marubinotto.util.Assert;

import org.apache.commons.lang.StringUtils;

public class PiggydbUtils {
	
	public static final String KEYWORD_DELIMITERS = 
		" \t\n\r\f+\"*%&/()=?'!,.;:-_@|^~`{}[]" + '\u3000';

	public static String[] splitToKeywords(String keywords) {
		Assert.Arg.notNull(keywords, "keywords");
		return StringUtils.split(keywords, KEYWORD_DELIMITERS);
	}
}

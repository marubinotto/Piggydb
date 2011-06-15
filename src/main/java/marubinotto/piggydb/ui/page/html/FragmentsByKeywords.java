package marubinotto.piggydb.ui.page.html;

import marubinotto.piggydb.util.PiggydbUtils;
import marubinotto.util.RegexUtils;

import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang.StringUtils;

public class FragmentsByKeywords extends AbstractFragments {
	
	public String keywords;
	public String keywordRegx;

	@Override 
	protected void setFragments() throws Exception {
		this.keywords = modifyIfGarbledByTomcat(this.keywords);
		
		if (StringUtils.isNotBlank(this.keywords)) {
			StringBuilder keywordRegx = new StringBuilder();
			for (String word : PiggydbUtils.splitToKeywords(this.keywords)) {
				if (keywordRegx.length() > 0) keywordRegx.append("|");
				word = StringEscapeUtils.escapeJavaScript(word);
				word = RegexUtils.escapeRegex(word);
				keywordRegx.append(word);
			}
			this.keywordRegx = "(" + keywordRegx.toString() + ")";
		}
		
		this.fragments = getFragmentRepository().findByKeywords(
			this.keywords, this.options);
	}
}

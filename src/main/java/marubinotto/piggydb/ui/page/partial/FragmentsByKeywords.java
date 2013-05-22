package marubinotto.piggydb.ui.page.partial;

import static org.apache.commons.lang.StringUtils.isNotBlank;
import marubinotto.piggydb.util.PiggydbUtils;
import marubinotto.util.RegexUtils;

import org.apache.commons.lang.StringEscapeUtils;

public class FragmentsByKeywords extends AbstractFragments {
	
	public String keywords;
	public String keywordRegx;

	@Override 
	protected void setFragments() throws Exception {
		this.keywords = modifyIfGarbledByTomcat(this.keywords);
		
		if (isNotBlank(this.keywords)) {
			// regex to match the keyword
			StringBuilder keywordRegx = new StringBuilder();
			for (String word : PiggydbUtils.splitToKeywords(this.keywords)) {
				if (keywordRegx.length() > 0) keywordRegx.append("|");
				word = StringEscapeUtils.escapeJavaScript(word);
				word = RegexUtils.escapeRegex(word);
				keywordRegx.append(word);
			}
			this.keywordRegx = "(" + keywordRegx.toString() + ")";
			
			// fragments label
			this.label = makeKeywordSearchLabel(this.keywords);
		}
		else {
		  this.label = getMessage("all");
		}
		
		marubinotto.piggydb.model.query.FragmentsByKeywords query = 
			(marubinotto.piggydb.model.query.FragmentsByKeywords)getQuery(
				marubinotto.piggydb.model.query.FragmentsByKeywords.class);
		query.setKeywords(this.keywords);
		this.fragments = getPage(query);
	}
}

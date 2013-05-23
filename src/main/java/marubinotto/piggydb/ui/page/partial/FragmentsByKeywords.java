package marubinotto.piggydb.ui.page.partial;

import static org.apache.commons.lang.StringUtils.isNotBlank;

public class FragmentsByKeywords extends AbstractFragments {
	
	public String keywords;

	@Override 
	protected void setFragments() throws Exception {
		this.keywords = modifyIfGarbledByTomcat(this.keywords);
		
		if (isNotBlank(this.keywords)) {
		  setKeywordRegex(this.keywords);
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

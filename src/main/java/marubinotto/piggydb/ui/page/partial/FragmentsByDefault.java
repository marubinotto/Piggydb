package marubinotto.piggydb.ui.page.partial;

import static org.apache.commons.lang.StringUtils.isNotBlank;
import marubinotto.piggydb.model.query.FragmentsAllButTrash;
import marubinotto.piggydb.model.query.FragmentsQuery;

public class FragmentsByDefault extends AbstractFragments {

	@Override 
	protected void setFragments() throws Exception {
	  this.queryable = true;
		FragmentsQuery query = getQuery(FragmentsAllButTrash.class);

		if (isNotBlank(this.query)) {
		  marubinotto.piggydb.model.query.FragmentsByKeywords queryByKeywords = 
	      (marubinotto.piggydb.model.query.FragmentsByKeywords)getQuery(
	        marubinotto.piggydb.model.query.FragmentsByKeywords.class);
		  queryByKeywords.setKeywords(this.query);
		  query = queryByKeywords;
		  
		  this.label = makeKeywordSearchLabel(this.query);
		}
		else {
		  this.label = getMessage("all");
		}
		
		this.fragments = getPage(query);
		if (this.fragments.getTotalSize() == 0) 
		  this.hideHeader = true;
	}
}

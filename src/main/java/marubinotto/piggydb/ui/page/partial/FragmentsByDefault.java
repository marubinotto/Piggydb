package marubinotto.piggydb.ui.page.partial;

import static org.apache.commons.lang.StringUtils.isBlank;
import static org.apache.commons.lang.StringUtils.isNotBlank;
import marubinotto.piggydb.model.query.FragmentsAllButTrash;
import marubinotto.piggydb.model.query.FragmentsQuery;

public class FragmentsByDefault extends AbstractFragments {

	@Override 
	protected void setFragments() throws Exception {
		FragmentsQuery query = getQuery(FragmentsAllButTrash.class);

		if (isNotBlank(this.query)) {
		  marubinotto.piggydb.model.query.FragmentsByKeywords queryByKeywords = 
	      (marubinotto.piggydb.model.query.FragmentsByKeywords)getQuery(
	        marubinotto.piggydb.model.query.FragmentsByKeywords.class);
		  queryByKeywords.setKeywords(this.query);
		  query = queryByKeywords;
		  
		  this.label = makeKeywordSearchLabel(this.query);
		  setKeywordRegex(this.query);
		}
		else {
		  this.label = getMessage("all");
		}
		
		this.fragments = getPage(query);
		if (this.fragments.getTotalSize() == 0 && isBlank(this.query)) 
		  this.hideHeader = true;
	}
}

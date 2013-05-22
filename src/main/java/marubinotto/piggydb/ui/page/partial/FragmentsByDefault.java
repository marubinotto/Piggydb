package marubinotto.piggydb.ui.page.partial;

import marubinotto.piggydb.model.query.FragmentsAllButTrash;
import marubinotto.piggydb.model.query.FragmentsQuery;

public class FragmentsByDefault extends AbstractFragments {

	@Override 
	protected void setFragments() throws Exception {
		FragmentsQuery query = getQuery(FragmentsAllButTrash.class);
		this.fragments = getPage(query);
		this.label = getMessage("all");
		if (this.fragments.getTotalSize() == 0) 
		  this.hideHeader = true;
	}
}

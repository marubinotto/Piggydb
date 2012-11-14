package marubinotto.piggydb.ui.page;

import static org.apache.commons.lang.StringUtils.isBlank;
import marubinotto.piggydb.ui.page.common.AbstractFragmentsPage;
import marubinotto.piggydb.ui.page.partial.FragmentsByBuiltinFilter;

public class BuiltinFilterPage extends AbstractFragmentsPage {

	public String name;
	
	@Override
	protected boolean onPreInit() throws Exception {	
		if (isBlank(this.name) || 
			!FragmentsByBuiltinFilter.queryTypes.containsKey(this.name)) {
			setRedirect(HomePage.class);
			return false;
		}
		return true;
	}
}

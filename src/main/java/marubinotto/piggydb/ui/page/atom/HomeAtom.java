package marubinotto.piggydb.ui.page.atom;

import java.util.List;

import marubinotto.piggydb.model.Fragment;
import marubinotto.piggydb.model.query.FragmentsAllButTrash;
import marubinotto.piggydb.model.query.FragmentsQuery;

public class HomeAtom extends AbstractAtom {

	@Override
	protected List<Fragment> getFragments() throws Exception {
		FragmentsQuery query = getQuery(FragmentsAllButTrash.class);
		return getPage(query);
	}
}

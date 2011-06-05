package marubinotto.piggydb.presentation.page.atom;

import java.util.List;

import marubinotto.piggydb.model.Fragment;

public class HomeAtom extends AbstractAtom {

	@Override
	protected List<Fragment> getFragments() throws Exception {
		return getFragmentRepository().getFragments(this.fragmentsOptions);
	}
}

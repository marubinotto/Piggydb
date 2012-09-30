package marubinotto.piggydb.model.query;

import java.util.Collection;

public interface FragmentsByIds extends FragmentsQuery {

	public void setIds(Collection<Long> fragmentIds);
}

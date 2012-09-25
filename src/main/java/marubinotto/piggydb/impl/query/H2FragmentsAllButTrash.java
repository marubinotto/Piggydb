package marubinotto.piggydb.impl.query;

import java.util.List;

import marubinotto.piggydb.model.query.FragmentsAllButTrash;

public class H2FragmentsAllButTrash 
extends H2FragmentsQueryBase implements FragmentsAllButTrash {

	protected void appendFromWhere(StringBuilder sql, List<Object> args) 
	throws Exception {
		sql.append("from fragment where 0 = 0");
		getRepository().appendConditionToExcludeTrash(sql, "fragment.fragment_id");
	}
}

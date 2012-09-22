package marubinotto.piggydb.impl.query;

import marubinotto.piggydb.model.query.FragmentsAllButTrash;
import marubinotto.util.paging.PageUtils;

public class H2FragmentsAllButTrash 
extends H2FragmentsQueryBase implements FragmentsAllButTrash {

	protected StringBuilder buildSql() throws Exception {
		StringBuilder sql = new StringBuilder();
		appendSelectAll(sql);
		sql.append(" from fragment where 0 = 0");
		appendConditionToExcludeTrash(sql, "fragment.fragment_id");
		return sql;
	}
	
	protected PageUtils.TotalCounter getTotalCounter() {
		return new PageUtils.TotalCounter() {
			public long getTotalSize() throws Exception {
				return getRepository().size();
			}
		};
	}
}

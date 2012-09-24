package marubinotto.piggydb.impl.query;

import java.util.List;

import marubinotto.piggydb.model.query.FragmentsAllButTrash;
import marubinotto.util.paging.PageUtils;

public class H2FragmentsAllButTrash 
extends H2FragmentsQueryBase implements FragmentsAllButTrash {

	protected void buildSql(StringBuilder sql, List<Object> args) throws Exception {
		appendSelectAll(sql);
		sql.append(" from fragment where 0 = 0");
		getRepository().appendConditionToExcludeTrash(sql, "fragment.fragment_id");
	}
	
	protected PageUtils.TotalCounter getTotalCounter() {
		return new PageUtils.TotalCounter() {
			public long getTotalSize() throws Exception {
				return getRepository().size();
			}
		};
	}
}

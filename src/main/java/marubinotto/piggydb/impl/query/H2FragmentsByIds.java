package marubinotto.piggydb.impl.query;

import static marubinotto.util.CollectionUtils.joinToString;

import java.util.Collection;
import java.util.List;

import marubinotto.piggydb.model.query.FragmentsByIds;
import marubinotto.util.Assert;

public class H2FragmentsByIds 
extends H2FragmentsQueryBase implements FragmentsByIds {

	private Collection<Long> fragmentIds;
	
	public void setIds(Collection<Long> fragmentIds) {
		this.fragmentIds = fragmentIds;
	}
	
	protected void appendFromWhere(StringBuilder sql, List<Object> args) 
	throws Exception {
		Assert.Property.requireNotNull(fragmentIds, "fragmentIds");
		
		sql.append("from fragment");
		sql.append(" where fragment_id in (");
		sql.append(joinToString(this.fragmentIds, ", "));
		sql.append(")");
	}
}

package marubinotto.piggydb.impl.query;

import java.util.List;

import marubinotto.piggydb.model.auth.OwnerAuth;
import marubinotto.piggydb.model.query.FragmentsByUser;
import marubinotto.util.Assert;

public class H2FragmentsByUser 
extends H2FragmentsQueryBase implements FragmentsByUser {
	
	private String userName;

	public void setUserName(String userName) {
		this.userName = userName;
	}
	
	protected void appendFromWhere(StringBuilder sql, List<Object> args) 
	throws Exception {
		Assert.Property.requireNotNull(userName, "userName");
		
		sql.append("from fragment");
		sql.append(" where (creator = ? or updater = ?");
		if (this.userName.equals(OwnerAuth.USER_NAME_OWNER)) {
			sql.append(" or creator is null");
			sql.append(" or (creation_datetime <> update_datetime and updater is null)");
		}
		sql.append(")");
		getRepository().appendConditionToExcludeTrash(sql, "fragment.fragment_id");
		
		args.add(this.userName);
		args.add(this.userName);
	}
}

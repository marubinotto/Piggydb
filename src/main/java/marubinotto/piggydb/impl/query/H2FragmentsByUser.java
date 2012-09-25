package marubinotto.piggydb.impl.query;

import java.util.List;

import marubinotto.piggydb.model.query.FragmentsByUser;
import marubinotto.util.Assert;

public class H2FragmentsByUser 
extends H2FragmentsQueryBase implements FragmentsByUser {
	
	private String userName;

	public void setUserName(String userName) {
		this.userName = userName;
	}
	
	protected void buildSelectFromWhereSql(StringBuilder sql, List<Object> args) 
	throws Exception {
		Assert.Property.requireNotNull(userName, "userName");
		
		
	}
}

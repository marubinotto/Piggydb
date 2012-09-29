package marubinotto.piggydb.model.query;

import marubinotto.piggydb.model.Fragment;

public interface FragmentsOfUser extends FragmentsQuery {
	
	public void setUserName(String userName);

	public Fragment getUserFragment() throws Exception;
}

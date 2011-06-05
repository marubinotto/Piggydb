package marubinotto.piggydb.model;

public interface Password {

	public boolean validatePassword(String password) throws Exception;
	
	public void changePassword(String password) throws Exception;
}

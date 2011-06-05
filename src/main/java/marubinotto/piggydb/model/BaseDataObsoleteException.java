package marubinotto.piggydb.model;

public class BaseDataObsoleteException extends Exception {

	public BaseDataObsoleteException() {
		super();
	}

	public BaseDataObsoleteException(String message) {
		super(message);
	}

	public BaseDataObsoleteException(String message, Throwable cause) {
		super(message, cause);
	}
}

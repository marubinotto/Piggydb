package marubinotto.piggydb.model.exception;

import marubinotto.util.message.CodedException;

public class InvalidTagNameException extends CodedException {

	public InvalidTagNameException(String errorCode) {
		super(errorCode, (String[])null);
	}

	public InvalidTagNameException(String errorCode, String field) {
		super(errorCode, new String[]{field});
	}
}

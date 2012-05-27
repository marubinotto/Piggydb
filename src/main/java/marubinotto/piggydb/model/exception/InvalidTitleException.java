package marubinotto.piggydb.model.exception;

import marubinotto.util.message.CodedException;

public class InvalidTitleException extends CodedException {

	public InvalidTitleException(String errorCode) {
		super(errorCode, (String[])null);
	}

	public InvalidTitleException(String errorCode, String field) {
		super(errorCode, new String[]{field});
	}
}

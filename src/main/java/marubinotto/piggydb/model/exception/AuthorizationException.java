package marubinotto.piggydb.model.exception;

import marubinotto.util.message.CodedException;

public class AuthorizationException extends CodedException {

	public AuthorizationException(String errorCode) {
		super(errorCode);
	}

	public AuthorizationException(String errorCode, String field) {
		super(errorCode, field);
	}
}

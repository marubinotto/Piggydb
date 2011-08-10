package marubinotto.piggydb.model.exception;

import marubinotto.util.CodedException;

public class DuplicateException extends CodedException {

	public DuplicateException(String errorCode) {
		super(errorCode, (String[])null);
	}

	public DuplicateException(String errorCode, String field) {
		super(errorCode, new String[]{field});
	}
}

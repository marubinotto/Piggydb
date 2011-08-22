package marubinotto.piggydb.model.exception;

import marubinotto.util.CodedException;

public class InvalidTaggingException extends CodedException {

	public InvalidTaggingException() {
		super("invalid-tagging");
	}
}

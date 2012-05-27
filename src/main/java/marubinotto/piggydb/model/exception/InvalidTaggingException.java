package marubinotto.piggydb.model.exception;

import marubinotto.util.message.CodedException;

public class InvalidTaggingException extends CodedException {

	public InvalidTaggingException() {
		super("invalid-tagging");
	}
}

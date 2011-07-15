package marubinotto.piggydb.model.exception;

import marubinotto.piggydb.model.Tag;
import marubinotto.util.CodedException;

public class InvalidTagNameException extends CodedException {

	public InvalidTagNameException() {
		super("invalid-tag-name", Tag.INVALID_CHARS);
	}
}

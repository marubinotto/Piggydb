package marubinotto.piggydb.model.exception;

import org.springframework.core.ErrorCoded;

public class InvalidTaggingException extends Exception implements ErrorCoded {

	public InvalidTaggingException() {
		super();
	}

	public String getErrorCode() {
		return "invalid-tagging";
	}
}

package marubinotto.util.web;

import marubinotto.util.MessageCode;
import marubinotto.util.MessageSource;

/**
 * This interface adds an option for the arguments to be HTML-escaped.
 * The arguments will be escaped by default as a defensive design.
 */
public interface WebMessageSource extends MessageSource {

	public String getMessage(String code, Object arg, boolean escapeArg);
	
	public String getMessage(String code, Object[] args, boolean escapeArgs);
	
	public String getMessage(MessageCode messageCode, boolean escapeArgs);
}

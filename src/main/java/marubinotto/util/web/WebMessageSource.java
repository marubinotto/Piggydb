package marubinotto.util.web;

import marubinotto.util.MessageSource;

public interface WebMessageSource extends MessageSource {

	public String getMessage(String code, Object arg, boolean escapeArg);
	
	public String getMessage(String code, Object[] args, boolean escapeArgs);
}

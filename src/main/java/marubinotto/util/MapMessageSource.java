package marubinotto.util;

import java.text.MessageFormat;
import java.util.Map;

public class MapMessageSource implements MessageSource {
	
	@SuppressWarnings("rawtypes")
	private Map messages;
	
	@SuppressWarnings("rawtypes")
	public MapMessageSource(Map messages) {
		Assert.Arg.notNull(messages, "messages");
		this.messages = messages;
	}

	public String getMessage(String code) {
		Assert.Arg.notNull(code, "code");
		return (String)this.messages.get(code);
	}

	public String getMessage(String code, Object arg) {
		Assert.Arg.notNull(code, "code");
		Assert.Arg.notNull(arg, "arg");
		
		return getMessage(code, new Object[]{arg});
	}

	public String getMessage(String code, Object[] args) {
		Assert.Arg.notNull(code, "code");
		Assert.Arg.notNull(args, "args");
		
		String value = getMessage(code);
		return MessageFormat.format(value, args);
	}
	
	public String getMessage(MessageCode messageCode) {
		Assert.Arg.notNull(messageCode, "messageCode");
		
		if (messageCode.getArguments() == null) {
			return getMessage(messageCode.getCode());
		}
		else {
			return getMessage(messageCode.getCode(), messageCode.getArguments());
		}
	}
}

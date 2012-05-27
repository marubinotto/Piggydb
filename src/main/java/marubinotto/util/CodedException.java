package marubinotto.util;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.builder.EqualsBuilder;

/**
 * marubinotto.util.CodedException
 */
public class CodedException extends RuntimeException implements MessageCode {

	private String code;
	private String[] arguments;

	public CodedException(String code) {
		this(code, (String[])null);
	}

	public CodedException(String code, String argument) {
		this(code, new String[]{argument});
	}

	public CodedException(String code, String[] arguments) {
		this.code = code;
		this.arguments = arguments;
	}

	public CodedException(String code, Throwable cause) {
		this(code, (String[])null, cause);
	}

	public CodedException(String code, String argument, Throwable cause) {
		this(code, new String[]{argument}, cause);
	}

	public CodedException(String code, String[] arguments, Throwable cause) {
		this.code = code;
		this.arguments = arguments;
	}

	public String getCode() {
		return this.code;
	}

	public Object[] getArguments() {
		return this.arguments;
	}

	public String getMessage() {
		if (this.arguments != null) {
			return this.code + " " + ArrayUtils.toString(this.arguments);
		}
		else {
			return this.code;
		}
	}
	
	public String getMessage(MessageSource source) {
		return source.getMessage(this);
	}

	@Override
	public boolean equals(Object object) {
		if (object == null) return false;
		if (object == this) return true;
		if (object.getClass() != getClass()) return false;

		CodedException theOther = (CodedException)object;
		return new EqualsBuilder()
			.append(this.code, theOther.code)
			.append(this.arguments, theOther.arguments)
			.isEquals();
	}
}

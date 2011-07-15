package marubinotto.util;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.springframework.core.ErrorCoded;

/**
 * marubinotto.util.CodedException
 */
public class CodedException extends RuntimeException implements ErrorCoded {

	private String errorCode;
	private String[] fields;

	public CodedException(String errorCode) {
		this(errorCode, (String[])null);
	}

	public CodedException(String errorCode, String field) {
		this(errorCode, new String[]{field});
	}

	public CodedException(String errorCode, String[] fields) {
		this.errorCode = errorCode;
		this.fields = fields;
	}

	public CodedException(String errorCode, Throwable cause) {
		this(errorCode, (String[])null, cause);
	}

	public CodedException(String errorCode, String field, Throwable cause) {
		this(errorCode, new String[]{field}, cause);
	}

	public CodedException(String errorCode, String[] fields, Throwable cause) {
		this.errorCode = errorCode;
		this.fields = fields;
	}

	public String getErrorCode() {
		return this.errorCode;
	}

	public String[] getFields() {
		return this.fields;
	}

	public String getMessage() {
		if (this.fields != null) {
			return this.errorCode + " " + ArrayUtils.toString(this.fields);
		}
		else {
			return this.errorCode;
		}
	}

	@Override
	public boolean equals(Object object) {
		if (object == null) return false;
		if (object == this) return true;
		if (object.getClass() != getClass()) return false;

		CodedException theOther = (CodedException)object;
		return new EqualsBuilder()
			.append(this.errorCode, theOther.errorCode)
			.append(this.fields, theOther.fields)
			.isEquals();
	}
}

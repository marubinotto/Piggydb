package marubinotto.util;

/**
 * marubinotto.util.AssertionFailedException
 */
public class AssertionFailedException extends RuntimeException {

	private Assert.Type assertionType;
	private String description;
	private Object actual;

	public AssertionFailedException(
		Assert.Type type, 
		String description, 
		Object actual) {
		
		this.assertionType = type;
		this.description = description;
		this.actual = actual;
	}

	public Assert.Type getAssertionType() {
		return this.assertionType;
	}

	public String getDescription() {
		return this.description;
	}

	public Object getActual() {
		return this.actual;
	}

	public String getMessage() {
		StringBuffer message = new StringBuffer();
		message.append(this.assertionType);
		if (this.description != null) {
			message.append(" <" + this.description + ">");
		}
		message.append(" violated");
		if (this.actual != null) {
			message.append(" (actual <" + this.actual + ">)");
		}
		message.append(" at <" + getCauseMethod() + ">");
		return new String(message);
	}

	public StackTraceElement getCauseStackTraceElement() {
		StackTraceElement[] elements = getStackTrace();
		for (int i = 0; i < elements.length; i++) {
			if (!elements[i].getClassName().startsWith(Assert.class.getName())) {
				return elements[i];
			}
		}
		return null;
	}

	public String getCauseMethod() {
		StackTraceElement cause = getCauseStackTraceElement();
		if (cause == null) {
			return null;
		}
		return cause.getClassName() + "#" + cause.getMethodName();
	}
}

package marubinotto.util;

/**
 * marubinotto.util.Assert
 */
public class Assert {

	public final static boolean ENABLE_ASSERTION = true;

	public static void require(boolean precondition, String description) {
		checkAssertion(precondition, description, null, Type.PRECONDITION);
	}

	public static void require(boolean precondition, String description, Object actual) {

		checkAssertion(precondition, description, actual, Type.PRECONDITION);
	}

	public static class Arg {
		public static void notNull(Object object, String argName) {
			checkAssertion(object != null, "arg: " + argName, null, Type.PRECONDITION_NOT_NULL);
		}
	}

	public static class Property {
		public static void requireNotNull(Object object, String propertyName) {
			checkAssertion(object != null, "property: " + propertyName, null, Type.PRECONDITION_NOT_NULL);
		}

		public static void ensureNotNull(Object object, String propertyName) {
			checkAssertion(object != null, "property: " + propertyName, null, Type.POSTCONDITION_NOT_NULL);
		}
	}

	public static void assertTrue(boolean condition, String description) {
		checkAssertion(condition, description, null, Type.ASSERTION);
	}

	private static void checkAssertion(
		boolean assertion, 
		String description, 
		Object actual, 
		Type assertionType) 
	throws AssertionFailedException {
		if (ENABLE_ASSERTION) {
			if (!assertion) {
				throw new AssertionFailedException(assertionType, description, actual);
			}
		}
	}

	public static class Type {
		public static final Type PRECONDITION = new Type("Precondition");
		public static final Type PRECONDITION_NOT_NULL = new Type("Precondition (Not null)");
		public static final Type POSTCONDITION = new Type("Postcondition");
		public static final Type POSTCONDITION_NOT_NULL = new Type("Postcondition (Not null)");
		public static final Type INVARIANT = new Type("Invariant");
		public static final Type ASSERTION = new Type("Assertion");
		public static final Type IMPOSSIBLE_CASE = new Type("Impossible case");

		private String name;

		public String toString() {
			return this.name;
		}

		private Type(String name) {
			this.name = name;
		}
	}
}

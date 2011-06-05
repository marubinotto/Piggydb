package marubinotto.util.velocity;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.velocity.tools.generic.introspection.PublicFieldUberspect;
import org.apache.velocity.util.introspection.Info;
import org.apache.velocity.util.introspection.VelMethod;

public class MarubinottoUberspect extends PublicFieldUberspect {
	
	private static Log logger = LogFactory.getLog(MarubinottoUberspect.class);

	public MarubinottoUberspect() {
	}

	@Override
	public VelMethod getMethod(Object obj, String methodName, Object[] args, Info info) 
	throws Exception {
		VelMethod method = super.getMethod(obj, methodName, args, info);
		if (method == null) {
			if (logger.isDebugEnabled()) {
				throw new RuntimeException(
					"Invalid method: " + methodName + toArgClasses(args) + " " + info);
			}
		}
		return method;
	}
	
	private static String toArgClasses(Object[] args) {
		if (args == null) return "";
		
		StringBuilder argClasses = new StringBuilder();
		argClasses.append("(");
		for (int i = 0; i < args.length; i++) {
			if (i > 0) argClasses.append(", ");
			argClasses.append(args[i].getClass().getName());
		}
		argClasses.append(")");
		return argClasses.toString();
	}
}

private String formatMethodCall() {
	Object[] args = invocation.getArguments();
	if (args == null || args.length == 0) {
		return invocation.getMethod().getName() + "()";
	}
	StringBuilder argsString = new StringBuilder();
	for (int i = 0; i < args.length; i++) {
		if (i > 0) {
			argsString.append(", ");
		}
		Object arg = args[i];
		if (arg == null) {
			argsString.append("null");
		} else if (arg instanceof String) {
			argsString.append("'").append(arg).append("'");
		} else if (arg.getClass().isArray()) {
			argsString.append("[");
			int length = java.lang.reflect.Array.getLength(arg);
			for (int j = 0; j < length; j++) {
				if (j > 0) argsString.append(", ");
				argsString.append(java.lang.reflect.Array.get(arg, j));
			}
			argsString.append("]");
		} else {
			argsString.append(arg.toString());
		}
	}
	return invocation.getMethod().getName() + "(" + argsString.toString() + ")";
}
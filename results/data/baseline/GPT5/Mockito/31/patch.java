private String formatMethodCall() {
	String methodName = (invocation != null && invocation.getMethod() != null) ? invocation.getMethod().getName() : "null";
	Object[] args = (invocation != null) ? invocation.getArguments() : null;
	String argsStr;
	if (args == null || args.length == 0) {
		argsStr = "";
	} else {
		String s = java.util.Arrays.deepToString(args);
		argsStr = (s.length() >= 2 && s.charAt(0) == '[' && s.charAt(s.length() - 1) == ']') ? s.substring(1, s.length() - 1) : s;
	}
	return methodName + "(" + argsStr + ")";
}
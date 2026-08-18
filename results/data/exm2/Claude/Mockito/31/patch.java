private String formatMethodCall() {
	Method method = invocation.getMethod();
	String methodName = method.getName();
	Object[] args = invocation.getArguments();
	
	if (args == null || args.length == 0) {
		return methodName + "()";
	}
	
	String argsStr = java.util.Arrays.stream(args)
		.map(arg -> arg == null ? "null" : arg.toString())
		.collect(java.util.stream.Collectors.joining(", "));
	return methodName + "(" + argsStr + ")";
}
private String formatMethodCall() {
	Method method = invocation.getMethod();
	Object[] args = invocation.getArguments();
	StringBuilder sb = new StringBuilder();
	sb.append(method.getName()).append("(");
	for (int i = 0; i < args.length; i++) {
		if (i > 0) {
			sb.append(", ");
		}
		sb.append(args[i]);
	}
	sb.append(")");
	return sb.toString();
}
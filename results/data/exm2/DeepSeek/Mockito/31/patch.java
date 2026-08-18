	private String formatMethodCall() {
		StringBuilder result = new StringBuilder();
		result.append(invocation.getMethod().getName());
		result.append("(");
		Object[] args = invocation.getArguments();
		for (int i = 0; i < args.length; i++) {
			if (i > 0) {
				result.append(", ");
			}
			result.append(args[i]);
		}
		result.append(")");
		return result.toString();
	}
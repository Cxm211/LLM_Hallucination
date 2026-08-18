	private String formatMethodCall() {
		Object[] args = invocation.getArguments();
		StringBuilder sb = new StringBuilder();
		sb.append(invocation.getMethod().getName());
		sb.append("(");
		for (int i = 0; i < args.length; i++) {
			if (i > 0) {
				sb.append(", ");
			}
			sb.append(args[i]);
		}
		sb.append(")");
		return sb.toString();
	}
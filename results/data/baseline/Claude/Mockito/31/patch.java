private String formatMethodCall() {
	if (invocation == null || invocation.getMethod() == null) {
		return "unknown()";
	}
	return invocation.getMethod().getName() + "()";
}
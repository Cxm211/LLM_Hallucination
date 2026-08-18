private String formatMethodCall() {
	return new org.mockito.internal.invocation.InvocationsPrinter().printInvocation(invocation);
}
private String formatMethodCall() {
	Method method = invocation.getMethod();
	String methodName = method.getName();
	
	if ("toString".equals(methodName) && method.getDeclaringClass() == Object.class) {
		return "toString()";
	}
	
	return methodName + "()";
}
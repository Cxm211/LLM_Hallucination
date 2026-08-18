private String formatMethodCall() {
    return invocation.getMockName() + "." + invocation.getMethod().getName() + "();";
}
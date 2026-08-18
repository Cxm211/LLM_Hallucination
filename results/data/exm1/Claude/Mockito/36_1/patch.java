public Object callRealMethod() throws Throwable {
    try {
        return realMethod.invoke(mock, rawArguments);
    } catch (java.lang.reflect.InvocationTargetException e) {
        Throwable cause = e.getCause();
        if (cause != null) {
            StackTraceElement[] filtered = new org.mockito.internal.exceptions.stacktrace.StackTraceFilter().filter(cause.getStackTrace(), false);
            cause.setStackTrace(filtered);
            throw cause;
        }
        throw e;
    }
}
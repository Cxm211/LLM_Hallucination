public Object callRealMethod() throws Throwable {
    try {
        Object[] args = rawArguments;
        if (args == null) {
            args = new Object[0];
        }
        return realMethod.invoke(mock, args);
    } catch (java.lang.reflect.InvocationTargetException e) {
        throw e.getCause();
    }
}
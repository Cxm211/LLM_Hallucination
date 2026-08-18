public Object callRealMethod() throws Throwable {
    try {
        return realMethod.invoke(mock, rawArguments);
    } catch (java.lang.reflect.InvocationTargetException e) {
        throw e.getCause();
    }
}
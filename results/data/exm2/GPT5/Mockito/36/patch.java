public Object callRealMethod() throws Throwable {
    if (realMethod == null) {
        throw new MockitoException("Cannot call real method on null");
    }
    Class<?> declaring = realMethod.getDeclaringClass();
    if (declaring.isInterface()) {
        throw new MockitoException("Cannot call real method on an interface");
    }
    try {
        return realMethod.invoke(mock, rawArguments);
    } catch (java.lang.reflect.InvocationTargetException e) {
        throw e.getTargetException();
    }
}
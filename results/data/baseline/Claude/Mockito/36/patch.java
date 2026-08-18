public Object callRealMethod() throws Throwable {
    if (mock == null) {
        throw new MockitoException("Cannot call real method on a null object");
    }
    if (realMethod == null) {
        throw new MockitoException("realMethod is null");
    }
    realMethod.setAccessible(true);
    return realMethod.invoke(mock, rawArguments);
}
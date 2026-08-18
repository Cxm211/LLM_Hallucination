public Object callRealMethod() throws Throwable {
    if (mock == null || realMethod == null) {
        throw new MockitoException("Cannot call real method on interface. Interface does not have any implementation!");
    }
    if (realMethod.getDeclaringClass().isInterface()) {
        throw new MockitoException("Cannot call real method on interface. Interface does not have any implementation!");
    }
    return realMethod.invoke(mock, rawArguments);
}
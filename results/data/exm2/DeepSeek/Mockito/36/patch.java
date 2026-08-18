    public Object callRealMethod() throws Throwable {
        if (realMethod.getDeclaringClass().isInterface()) {
            throw new MockitoException("Cannot call real method on interface");
        }
        return realMethod.invoke(mock, rawArguments);
    }
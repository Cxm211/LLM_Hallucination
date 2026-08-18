    public Object callRealMethod() throws Throwable {
        if (realMethod == null) {
            throw new MockitoException("Real method cannot be called on interface mock.");
        }
        return realMethod.invoke(mock, rawArguments);
    }
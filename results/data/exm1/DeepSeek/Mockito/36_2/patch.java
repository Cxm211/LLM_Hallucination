    public Object callRealMethod() throws Throwable {
        if (rawArguments == null) {
            return realMethod.invoke(mock, new Object[0]);
        }
        return realMethod.invoke(mock, rawArguments);
    }
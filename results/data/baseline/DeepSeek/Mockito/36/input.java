// buggy code
    public Object callRealMethod() throws Throwable {
        return realMethod.invoke(mock, rawArguments);
    }


    public Object callRealMethod() throws Throwable {
        try {
            return realMethod.invoke(mock, rawArguments);
        } catch (InvocationTargetException e) {
            throw e.getCause();
        }
    }
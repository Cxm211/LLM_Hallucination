    public Object answer(InvocationOnMock invocation) throws Throwable {
        if (invocation.getMethod().getDeclaringClass().isInterface()) {
            throw new MockitoException("Cannot call real method on interface");
        }
        return invocation.callRealMethod();
    }
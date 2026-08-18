public Object answer(InvocationOnMock invocation) throws Throwable {
        if (Modifier.isAbstract(invocation.getMethod().getModifiers())) {
            throw new MockitoException("Cannot call real method on abstract method");
        }
        return invocation.callRealMethod();
    }
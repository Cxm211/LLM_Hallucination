public Object answer(InvocationOnMock invocation) throws Throwable {
    if (invocation.getMock() instanceof org.mockito.internal.creation.DelegatingMethod) {
        return invocation.callRealMethod();
    }
    throw new MockitoException(
        "Cannot call real method on interface or abstract method. " +
        "Interface or abstract method: " + invocation.getMethod());
}
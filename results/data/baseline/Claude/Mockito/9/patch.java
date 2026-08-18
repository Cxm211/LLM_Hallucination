public Object answer(InvocationOnMock invocation) throws Throwable {
    if (invocation.getMock() == null) {
        throw new MockitoException("Cannot call real method on null object");
    }
    return invocation.callRealMethod();
}
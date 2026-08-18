public Object answer(InvocationOnMock invocation) throws Throwable {
    if (invocation.getMock() instanceof Class) {
        throw new MockitoException("Cannot call real method on interface-based mock or class mock without a real instance.");
    }
    return invocation.callRealMethod();
}
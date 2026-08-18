public Object answer(InvocationOnMock invocation) throws Throwable {
    if (invocation.getMock().getClass().isInterface() || invocation.getMethod().getDeclaringClass().isInterface()) {
        throw new MockitoException(
            "\nCannot call real method on interface based mock.\n" +
            "Interface does not have any implementation!\n" +
            "Calling real methods is only possible when mocking concrete classes.");
    }
    return invocation.callRealMethod();
}
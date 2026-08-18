public Object answer(InvocationOnMock invocation) throws Throwable {
    if (invocation.getMethod().getDeclaringClass().isAssignableFrom(invocation.getMock().getClass())) {
        return invocation.callRealMethod();
    }
    throw new MockitoException("Cannot call real method on interface or abstract class: " + invocation.getMethod());
}
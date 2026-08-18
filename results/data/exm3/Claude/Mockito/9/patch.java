public Object answer(InvocationOnMock invocation) throws Throwable {
    if (invocation.getMethod().isAbstract()) {
        Class<?> returnType = invocation.getMethod().getReturnType();
        return org.mockito.internal.util.Primitives.defaultValue(returnType);
    }
    return invocation.callRealMethod();
}
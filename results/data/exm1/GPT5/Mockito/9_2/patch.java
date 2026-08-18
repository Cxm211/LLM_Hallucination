public Object answer(InvocationOnMock invocation) throws Throwable {
        java.lang.reflect.Method method = invocation.getMethod();
        if (method.getDeclaringClass().isInterface() || java.lang.reflect.Modifier.isAbstract(method.getModifiers())) {
            throw new MockitoException("Cannot call real method on interface or abstract method");
        }
        return invocation.callRealMethod();
    }
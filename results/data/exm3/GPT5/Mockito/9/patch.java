public Object answer(InvocationOnMock invocation) throws Throwable {
        java.lang.reflect.Method method = invocation.getMethod();
        if (java.lang.reflect.Modifier.isAbstract(method.getModifiers())) {
            return Answers.RETURNS_DEFAULTS.answer(invocation);
        }
        return invocation.callRealMethod();
    }
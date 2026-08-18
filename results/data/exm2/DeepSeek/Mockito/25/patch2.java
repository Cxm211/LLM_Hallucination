    private Object recordDeepStubMock(InvocationOnMock invocation, InvocationContainerImpl container) {
        Class<?> clz = invocation.getMethod().getReturnType();
        if (!new MockCreationValidator().isTypeMockable(clz)) {
            Object defaultValue = delegate.returnValueFor(clz);
            container.addAnswer(new Answer<Object>() {
                public Object answer(InvocationOnMock invocation) throws Throwable {
                    return defaultValue;
                }
            }, false);
            return defaultValue;
        }
        final Object mock = org.mockito.Mockito.mock(clz, this);

        container.addAnswer(new Answer<Object>() {
            public Object answer(InvocationOnMock invocation) throws Throwable {
                return mock;
            }
        }, false);

        return mock;
    }
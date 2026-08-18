private Object recordDeepStubMock(InvocationOnMock invocation, InvocationContainerImpl container) {
        // Resolve the most specific raw type from generics for proper deep stubbing
        GenericMetadataSupport returnTypeGenericMetadata =
                actualParameterizedType(invocation.getMock()).resolveGenericReturnType(invocation.getMethod());
        Class<?> clz = returnTypeGenericMetadata.rawType();
        final Object mock = org.mockito.Mockito.mock(clz, this);

        container.addAnswer(new Answer<Object>() {
            public Object answer(InvocationOnMock invocation) throws Throwable {
                return mock;
            }
        }, false);

        return mock;
    }
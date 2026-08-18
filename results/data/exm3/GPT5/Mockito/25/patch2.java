    private Object recordDeepStubMock(InvocationOnMock invocation, InvocationContainerImpl container) {
        // resolve generic return type to determine raw type and any additional interfaces from bounds
        GenericMetadataSupport returnTypeGenericMetadata =
                actualParameterizedType(invocation.getMock()).resolveGenericReturnType(invocation.getMethod());
        Class<?> rawType = returnTypeGenericMetadata.rawType();

        org.mockito.MockSettings settings = org.mockito.Mockito.withSettings().defaultAnswer(this);
        try {
            // If GenericMetadataSupport exposes extra interfaces for multiple bounds, add them
            Class<?>[] extra = returnTypeGenericMetadata.rawExtraInterfaces();
            if (extra != null && extra.length > 0) {
                settings = settings.extraInterfaces(extra);
            }
        } catch (Throwable ignored) {
            // Be defensive in case the metadata implementation doesn't expose extra interfaces
        }

        final Object mock = org.mockito.Mockito.mock(rawType, settings);

        container.addAnswer(new Answer<Object>() {
            public Object answer(InvocationOnMock invocation) throws Throwable {
                return mock;
            }
        }, false);

        return mock;
    }
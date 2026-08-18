public Object answer(InvocationOnMock invocation) throws Throwable {
        GenericMetadataSupport returnTypeGenericMetadata =
                actualParameterizedType(invocation.getMock()).resolveGenericReturnType(invocation.getMethod());

        if (returnTypeGenericMetadata == null) {
            Class<?> fallbackType = invocation.getMethod().getReturnType();
            if (!mockitoCore.isTypeMockable(fallbackType)) {
                return delegate.returnValueFor(fallbackType);
            }
            return getMock(invocation, actualParameterizedType(invocation.getMock()).resolveGenericReturnType(invocation.getMethod()));
        }

        Class<?> rawType = returnTypeGenericMetadata.rawType();
        if (rawType == null || !mockitoCore.isTypeMockable(rawType)) {
            return delegate.returnValueFor(rawType != null ? rawType : invocation.getMethod().getReturnType());
        }

        return getMock(invocation, returnTypeGenericMetadata);
    }
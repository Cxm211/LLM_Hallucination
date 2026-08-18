public Object answer(InvocationOnMock invocation) throws Throwable {
        GenericMetadataSupport returnTypeGenericMetadata =
                actualParameterizedType(invocation.getMock()).resolveGenericReturnType(invocation.getMethod());

        Class<?> rawType = returnTypeGenericMetadata.rawType();
        if (!mockitoCore.isTypeMockable(rawType)) {
            // Delegate full invocation to preserve contextual behavior (e.g., special cases like compareTo, arrays, smart nulls)
            return delegate.answer(invocation);
        }

        return getMock(invocation, returnTypeGenericMetadata);
    }
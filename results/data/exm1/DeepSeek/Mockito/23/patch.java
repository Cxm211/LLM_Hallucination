public Object answer(InvocationOnMock invocation) throws Throwable {
    GenericMetadataSupport returnTypeGenericMetadata =
            actualParameterizedType(invocation.getMock()).resolveGenericReturnType(invocation.getMethod());
    if (returnTypeGenericMetadata == null) {
        return delegate.returnValueFor(invocation.getMethod().getReturnType());
    }
    Class<?> rawType = returnTypeGenericMetadata.rawType();
    if (!mockitoCore.isTypeMockable(rawType)) {
        return delegate.returnValueFor(rawType);
    }
    return getMock(invocation, returnTypeGenericMetadata);
}
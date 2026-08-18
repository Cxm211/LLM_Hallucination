private Object deepStub(InvocationOnMock invocation, GenericMetadataSupport returnTypeGenericMetadata) throws Throwable {
    InternalMockHandler<Object> handler = new MockUtil().getMockHandler(invocation.getMock());
    InvocationContainerImpl container = (InvocationContainerImpl) handler.getInvocationContainer();

    // matches invocation for verification
    Invocation actualInvocation = invocation.getInvocation();
    for (StubbedInvocationMatcher stubbedInvocationMatcher : container.getStubbedInvocations()) {
        if (actualInvocation.matches(stubbedInvocationMatcher.getInvocation())) {
            return stubbedInvocationMatcher.answer(invocation);
        }
    }

    // record deep stub answer
    return recordDeepStubAnswer(
            newDeepStubMock(returnTypeGenericMetadata),
            container
    );
}
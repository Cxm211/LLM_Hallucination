private Object deepStub(InvocationOnMock invocation, GenericMetadataSupport returnTypeGenericMetadata) throws Throwable {
    InternalMockHandler<Object> handler = new MockUtil().getMockHandler(invocation.getMock());
    InvocationContainerImpl container = (InvocationContainerImpl) handler.getInvocationContainer();

    // matches invocation for verification
    for (StubbedInvocationMatcher stubbedInvocationMatcher : container.getStubbedInvocations()) {
        if (stubbedInvocationMatcher.matches(invocation)) {
            return stubbedInvocationMatcher.answer(invocation);
        }
    }

    // record deep stub answer
    return recordDeepStubAnswer(
            newDeepStubMock(returnTypeGenericMetadata),
            container
    );
}
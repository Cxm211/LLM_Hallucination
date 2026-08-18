private Object deepStub(InvocationOnMock invocation, GenericMetadataSupport returnTypeGenericMetadata) throws Throwable {
    InternalMockHandler<Object> handler = new MockUtil().getMockHandler(invocation.getMock());
    InvocationContainerImpl container = (InvocationContainerImpl) handler.getInvocationContainer();

    // record deep stub answer
    return recordDeepStubAnswer(
            newDeepStubMock(returnTypeGenericMetadata),
            container
    );
}
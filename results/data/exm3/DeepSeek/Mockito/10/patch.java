    private Object deepStub(InvocationOnMock invocation, GenericMetadataSupport returnTypeGenericMetadata) throws Throwable {
        InternalMockHandler<Object> handler = new MockUtil().getMockHandler(invocation.getMock());
        InvocationContainerImpl container = (InvocationContainerImpl) handler.getInvocationContainer();

        // matches invocation for verification
        for (StubbedInvocationMatcher stubbedInvocationMatcher : container.getStubbedInvocations()) {
            if (container.getInvocationForStubbing().matches(stubbedInvocationMatcher.getInvocation())) {
                return stubbedInvocationMatcher.answer(invocation);
            }
        }

        // Determine if the original mock is serializable
        MockSettings originalMockSettings = new MockUtil().getMockSettings(invocation.getMock());
        boolean isSerializable = originalMockSettings.isSerializable();

        // record deep stub answer
        return recordDeepStubAnswer(
                newDeepStubMock(returnTypeGenericMetadata, isSerializable),
                container
        );
    }
    private Object getMock(InvocationOnMock invocation) throws Throwable {
        InternalMockHandler<Object> handler = new MockUtil().getMockHandler(invocation.getMock());
        InvocationContainerImpl container = (InvocationContainerImpl) handler.getInvocationContainer();

        if (container.getInvocationForStubbing() != null) {
            for (StubbedInvocationMatcher stubbedInvocationMatcher : container.getStubbedInvocations()) {
                if (container.getInvocationForStubbing().matches(stubbedInvocationMatcher.getInvocation())) {
                    return stubbedInvocationMatcher.answer(invocation);
                }
            }
        }

        // deep stub
        return recordDeepStubMock(invocation, container);
    }
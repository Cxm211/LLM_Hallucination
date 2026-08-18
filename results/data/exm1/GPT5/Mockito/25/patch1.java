private Object getMock(InvocationOnMock invocation) throws Throwable {
    	InternalMockHandler<Object> handler = new MockUtil().getMockHandler(invocation.getMock());
    	InvocationContainerImpl container = (InvocationContainerImpl) handler.getInvocationContainer();

        // matches invocation for verification
        InvocationMatcher invocationForStubbing = container.getInvocationForStubbing();
        for (StubbedInvocationMatcher stubbedInvocationMatcher : container.getStubbedInvocations()) {
    		if(invocationForStubbing != null && invocationForStubbing.matches(stubbedInvocationMatcher.getInvocation())) {
    			return stubbedInvocationMatcher.answer(invocation);
    		}
		}

        // deep stub
        return recordDeepStubMock(invocation, container);
    }
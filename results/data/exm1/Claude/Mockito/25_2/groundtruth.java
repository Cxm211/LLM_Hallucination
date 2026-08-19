// ===== FIXED org.mockito.internal.stubbing.defaultanswers.ReturnsDeepStubs :: answer(InvocationOnMock) [lines 47-57] from /Users/grace/Documents/Paper/BugFixing/Interpretation/defects4j_fixed/Mockito/Mockito-25-fixed/src/org/mockito/internal/stubbing/defaultanswers/ReturnsDeepStubs.java =====
    public Object answer(InvocationOnMock invocation) throws Throwable {
        GenericMetadataSupport returnTypeGenericMetadata =
                actualParameterizedType(invocation.getMock()).resolveGenericReturnType(invocation.getMethod());

        Class<?> rawType = returnTypeGenericMetadata.rawType();
        if (!new MockCreationValidator().isTypeMockable(rawType)) {
            return delegate.returnValueFor(rawType);
        }

        return getMock(invocation, returnTypeGenericMetadata);
    }

// ===== FIXED org.mockito.internal.stubbing.defaultanswers.ReturnsDeepStubs :: getMock(InvocationOnMock) [lines 59-72] from /Users/grace/Documents/Paper/BugFixing/Interpretation/defects4j_fixed/Mockito/Mockito-25-fixed/src/org/mockito/internal/stubbing/defaultanswers/ReturnsDeepStubs.java =====
    private Object getMock(InvocationOnMock invocation, GenericMetadataSupport returnTypeGenericMetadata) throws Throwable {
    	InternalMockHandler<Object> handler = new MockUtil().getMockHandler(invocation.getMock());
    	InvocationContainerImpl container = (InvocationContainerImpl) handler.getInvocationContainer();

        // matches invocation for verification
        for (StubbedInvocationMatcher stubbedInvocationMatcher : container.getStubbedInvocations()) {
    		if(container.getInvocationForStubbing().matches(stubbedInvocationMatcher.getInvocation())) {
    			return stubbedInvocationMatcher.answer(invocation);
    		}
		}

        // deep stub
        return recordDeepStubMock(createNewDeepStubMock(returnTypeGenericMetadata), container);
    }

// ===== FIXED org.mockito.internal.stubbing.defaultanswers.ReturnsDeepStubs :: recordDeepStubMock(InvocationOnMock, InvocationContainerImpl) [lines 106-115] from /Users/grace/Documents/Paper/BugFixing/Interpretation/defects4j_fixed/Mockito/Mockito-25-fixed/src/org/mockito/internal/stubbing/defaultanswers/ReturnsDeepStubs.java =====
    private Object recordDeepStubMock(final Object mock, InvocationContainerImpl container) throws Throwable {

        container.addAnswer(new Answer<Object>() {
            public Object answer(InvocationOnMock invocation) throws Throwable {
                return mock;
            }
        }, false);

        return mock;
    }

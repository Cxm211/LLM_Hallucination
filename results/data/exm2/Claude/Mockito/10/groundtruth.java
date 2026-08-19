// ===== FIXED org.mockito.internal.stubbing.defaultanswers.ReturnsDeepStubs :: deepStub(InvocationOnMock, GenericMetadataSupport) [lines 59-75] from /Users/grace/Documents/Paper/BugFixing/Interpretation/defects4j_fixed/Mockito/Mockito-10-fixed/src/org/mockito/internal/stubbing/defaultanswers/ReturnsDeepStubs.java =====
    private Object deepStub(InvocationOnMock invocation, GenericMetadataSupport returnTypeGenericMetadata) throws Throwable {
        InternalMockHandler<Object> handler = new MockUtil().getMockHandler(invocation.getMock());
        InvocationContainerImpl container = (InvocationContainerImpl) handler.getInvocationContainer();

        // matches invocation for verification
        for (StubbedInvocationMatcher stubbedInvocationMatcher : container.getStubbedInvocations()) {
            if (container.getInvocationForStubbing().matches(stubbedInvocationMatcher.getInvocation())) {
                return stubbedInvocationMatcher.answer(invocation);
            }
        }

        // record deep stub answer
        return recordDeepStubAnswer(
                newDeepStubMock(returnTypeGenericMetadata, invocation.getMock()),
                container
        );
    }

// ===== FIXED org.mockito.internal.stubbing.defaultanswers.ReturnsDeepStubs :: newDeepStubMock(GenericMetadataSupport) [lines 88-94] from /Users/grace/Documents/Paper/BugFixing/Interpretation/defects4j_fixed/Mockito/Mockito-10-fixed/src/org/mockito/internal/stubbing/defaultanswers/ReturnsDeepStubs.java =====
    private Object newDeepStubMock(GenericMetadataSupport returnTypeGenericMetadata, Object parentMock) {
        MockCreationSettings parentMockSettings = new MockUtil().getMockSettings(parentMock);
        return mockitoCore().mock(
                returnTypeGenericMetadata.rawType(),
                withSettingsUsing(returnTypeGenericMetadata, parentMockSettings)
        );
    }

// ===== FIXED org.mockito.internal.stubbing.defaultanswers.ReturnsDeepStubs :: withSettingsUsing(GenericMetadataSupport) [lines 96-103] from /Users/grace/Documents/Paper/BugFixing/Interpretation/defects4j_fixed/Mockito/Mockito-10-fixed/src/org/mockito/internal/stubbing/defaultanswers/ReturnsDeepStubs.java =====
    private MockSettings withSettingsUsing(GenericMetadataSupport returnTypeGenericMetadata, MockCreationSettings parentMockSettings) {
        MockSettings mockSettings = returnTypeGenericMetadata.hasRawExtraInterfaces() ?
                withSettings().extraInterfaces(returnTypeGenericMetadata.rawExtraInterfaces())
                : withSettings();

        return propagateSerializationSettings(mockSettings, parentMockSettings)
                .defaultAnswer(returnsDeepStubsAnswerUsing(returnTypeGenericMetadata));
    }

// ===== FIXED org.mockito.internal.stubbing.defaultanswers.ReturnsDeepStubs :: answer(InvocationOnMock) [lines 47-59] from /Users/grace/Documents/Paper/BugFixing/Interpretation/defects4j_fixed/Mockito/Mockito-23-fixed/src/org/mockito/internal/stubbing/defaultanswers/ReturnsDeepStubs.java =====
    public Object answer(InvocationOnMock invocation) throws Throwable {
        GenericMetadataSupport returnTypeGenericMetadata =
                actualParameterizedType(invocation.getMock()).resolveGenericReturnType(invocation.getMethod());

        Class<?> rawType = returnTypeGenericMetadata.rawType();
        instantiateMockitoCoreIfNeeded();
        instantiateDelegateIfNeeded();
        if (!mockitoCore.isTypeMockable(rawType)) {
            return delegate.returnValueFor(rawType);
        }

        return getMock(invocation, returnTypeGenericMetadata);
    }

// ===== FIXED org.mockito.internal.stubbing.defaultanswers.ReturnsDeepStubs :: recordDeepStubMock(Object, InvocationContainerImpl) [lines 125-134] from /Users/grace/Documents/Paper/BugFixing/Interpretation/defects4j_fixed/Mockito/Mockito-23-fixed/src/org/mockito/internal/stubbing/defaultanswers/ReturnsDeepStubs.java =====
    private Object recordDeepStubMock(final Object mock, InvocationContainerImpl container) throws Throwable {

        container.addAnswer(new SerializableAnswer() {
            public Object answer(InvocationOnMock invocation) throws Throwable {
                return mock;
            }
        }, false);

        return mock;
    }

// ===== FIXED org.mockito.internal.stubbing.defaultanswers.ReturnsDeepStubs :: withSettingsUsing(GenericMetadataSupport) [lines 105-114] from /Users/grace/Documents/Paper/BugFixing/Interpretation/defects4j_fixed/Mockito/Mockito-23-fixed/src/org/mockito/internal/stubbing/defaultanswers/ReturnsDeepStubs.java =====
    private MockSettings withSettingsUsing(GenericMetadataSupport returnTypeGenericMetadata) {
        MockSettings mockSettings =
                returnTypeGenericMetadata.rawExtraInterfaces().length > 0 ?
                withSettings().extraInterfaces(returnTypeGenericMetadata.rawExtraInterfaces())
                : withSettings();

        return mockSettings
		        .serializable()
                .defaultAnswer(returnsDeepStubsAnswerUsing(returnTypeGenericMetadata));
    }

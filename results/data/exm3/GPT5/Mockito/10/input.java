// buggy function
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
                newDeepStubMock(returnTypeGenericMetadata),
                container
        );
    }

    private Object newDeepStubMock(GenericMetadataSupport returnTypeGenericMetadata) {
        return mockitoCore().mock(
                returnTypeGenericMetadata.rawType(),
                withSettingsUsing(returnTypeGenericMetadata)
        );
    }

    private MockSettings withSettingsUsing(GenericMetadataSupport returnTypeGenericMetadata) {
        MockSettings mockSettings = returnTypeGenericMetadata.hasRawExtraInterfaces() ?
                withSettings().extraInterfaces(returnTypeGenericMetadata.rawExtraInterfaces())
                : withSettings();

        return mockSettings.serializable()
                .defaultAnswer(returnsDeepStubsAnswerUsing(returnTypeGenericMetadata));
    }

// trigger testcase
// org/mockitousage/bugs/DeepStubsWronglyReportsSerializationProblemsTest.java::should_not_raise_a_mockito_exception_about_serialization_when_accessing_deep_stub
@Test
    public void should_not_raise_a_mockito_exception_about_serialization_when_accessing_deep_stub() {
        NotSerializableShouldBeMocked the_deep_stub = mock(ToBeDeepStubbed.class, RETURNS_DEEP_STUBS).getSomething();
        assertThat(the_deep_stub).isNotNull();
    }

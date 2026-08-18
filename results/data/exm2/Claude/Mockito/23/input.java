    public Object answer(InvocationOnMock invocation) throws Throwable {
        GenericMetadataSupport returnTypeGenericMetadata =
                actualParameterizedType(invocation.getMock()).resolveGenericReturnType(invocation.getMethod());

        Class<?> rawType = returnTypeGenericMetadata.rawType();
        if (!mockitoCore.isTypeMockable(rawType)) {
            return delegate.returnValueFor(rawType);
        }

        return getMock(invocation, returnTypeGenericMetadata);
    }

    private MockSettings withSettingsUsing(GenericMetadataSupport returnTypeGenericMetadata) {
        MockSettings mockSettings =
                returnTypeGenericMetadata.rawExtraInterfaces().length > 0 ?
                withSettings().extraInterfaces(returnTypeGenericMetadata.rawExtraInterfaces())
                : withSettings();

        return mockSettings
                .defaultAnswer(returnsDeepStubsAnswerUsing(returnTypeGenericMetadata));
    }

    private Object recordDeepStubMock(final Object mock, InvocationContainerImpl container) throws Throwable {

        container.addAnswer(new Answer<Object>() {
            public Object answer(InvocationOnMock invocation) throws Throwable {
                return mock;
            }
        }, false);

        return mock;
    }

// trigger testcase
@Test
    public void should_serialize_and_deserialize_mock_created_by_deep_stubs() throws Exception {
        // given
        SampleClass sampleClass = mock(SampleClass.class, withSettings().defaultAnswer(Mockito.RETURNS_DEEP_STUBS).serializable());
        when(sampleClass.getSample().isSth()).thenReturn(STUBBED_BOOLEAN_VALUE);
        when(sampleClass.getSample().getNumber()).thenReturn(STUBBED_INTEGER_VALUE);

        // when
        Object o = SimpleSerializationUtil.serializeAndBack(sampleClass);

        // then
        assertThat(o).isInstanceOf(SampleClass.class);
        SampleClass deserializedSample = (SampleClass) o;
        assertThat(deserializedSample.getSample().isSth()).isEqualTo(STUBBED_BOOLEAN_VALUE);
        assertThat(deserializedSample.getSample().getNumber()).isEqualTo(STUBBED_INTEGER_VALUE);
    }

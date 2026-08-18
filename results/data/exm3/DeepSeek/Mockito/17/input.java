// buggy function
    public MockSettings serializable() {
        return this.extraInterfaces(java.io.Serializable.class);
    }

    public boolean isSerializable() {
        return extraInterfaces != null && java.util.Arrays.asList(extraInterfaces).contains(java.io.Serializable.class);
    }

    public <T> T createMock(Class<T> classToMock, MockSettingsImpl settings) {
        creationValidator.validateType(classToMock);
        creationValidator.validateExtraInterfaces(classToMock, settings.getExtraInterfaces());
        creationValidator.validateMockedType(classToMock, settings.getSpiedInstance());

        settings.initiateMockName(classToMock);

        MockHandler<T> mockHandler = new MockHandler<T>(settings);
        MethodInterceptorFilter filter = new MethodInterceptorFilter(mockHandler, settings);
        Class<?>[] interfaces = settings.getExtraInterfaces();

        Class<?>[] ancillaryTypes;
            ancillaryTypes = interfaces == null ? new Class<?>[0] : interfaces;

        Object spiedInstance = settings.getSpiedInstance();
        
        T mock = ClassImposterizer.INSTANCE.imposterise(filter, classToMock, ancillaryTypes);
        
        if (spiedInstance != null) {
            new LenientCopyTool().copyToMock(spiedInstance, mock);
        }
        
        return mock;
    }

// trigger testcase
// org/mockitousage/basicapi/MocksSerializationTest.java::shouldBeSerializeAndHaveExtraInterfaces
@Test
    public void shouldBeSerializeAndHaveExtraInterfaces() throws Exception {
        //when
        IMethods mock = mock(IMethods.class, withSettings().serializable().extraInterfaces(List.class));
        IMethods mockTwo = mock(IMethods.class, withSettings().extraInterfaces(List.class).serializable());

        //then
        serializeAndBack((List) mock);
        serializeAndBack((List) mockTwo);
    }

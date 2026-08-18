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

        // ensure Serializable is implemented if requested, regardless of extraInterfaces configuration
        if (settings.isSerializable()) {
            boolean alreadySerializable = false;
            for (Class<?> itf : ancillaryTypes) {
                if (java.io.Serializable.class.equals(itf)) { alreadySerializable = true; break; }
            }
            if (!alreadySerializable) {
                Class<?>[] merged = java.util.Arrays.copyOf(ancillaryTypes, ancillaryTypes.length + 1);
                merged[ancillaryTypes.length] = java.io.Serializable.class;
                ancillaryTypes = merged;
            }
        }

        Object spiedInstance = settings.getSpiedInstance();
        
        T mock = ClassImposterizer.INSTANCE.imposterise(filter, classToMock, ancillaryTypes);
        
        if (spiedInstance != null) {
            new LenientCopyTool().copyToMock(spiedInstance, mock);
        }
        
        return mock;
    }
    public <T> T createMock(Class<T> classToMock, MockSettingsImpl settings) {
        creationValidator.validateType(classToMock);
        creationValidator.validateExtraInterfaces(classToMock, settings.getExtraInterfaces());
        creationValidator.validateMockedType(classToMock, settings.getSpiedInstance());

        settings.initiateMockName(classToMock);

        MockHandler<T> mockHandler = new MockHandler<T>(settings);
        MethodInterceptorFilter filter = new MethodInterceptorFilter(mockHandler, settings);
        Class<?>[] interfaces = settings.getExtraInterfaces();

        // Ensure Serializable is included if settings indicate serializable
        boolean hasSerializable = false;
        if (interfaces != null) {
            for (Class<?> iface : interfaces) {
                if (iface == java.io.Serializable.class) {
                    hasSerializable = true;
                    break;
                }
            }
        }
        if (settings.isSerializable() && !hasSerializable) {
            int newLength = (interfaces == null ? 0 : interfaces.length) + 1;
            Class<?>[] newInterfaces = new Class<?>[newLength];
            if (interfaces != null) {
                System.arraycopy(interfaces, 0, newInterfaces, 0, interfaces.length);
            }
            newInterfaces[newLength - 1] = java.io.Serializable.class;
            interfaces = newInterfaces;
        }

        Class<?>[] ancillaryTypes;
            ancillaryTypes = interfaces == null ? new Class<?>[0] : interfaces;

        Object spiedInstance = settings.getSpiedInstance();
        
        T mock = ClassImposterizer.INSTANCE.imposterise(filter, classToMock, ancillaryTypes);
        
        if (spiedInstance != null) {
            new LenientCopyTool().copyToMock(spiedInstance, mock);
        }
        
        return mock;
    }
    public <T> T createMock(Class<T> classToMock, MockSettingsImpl settings) {
        creationValidator.validateType(classToMock);
        creationValidator.validateExtraInterfaces(classToMock, settings.getExtraInterfaces());
        creationValidator.validateMockedType(classToMock, settings.getSpiedInstance());

        settings.initiateMockName(classToMock);

        MockHandler<T> mockHandler = new MockHandler<T>(settings);
        MethodInterceptorFilter filter = new MethodInterceptorFilter(mockHandler, settings);
        Class<?>[] interfaces = settings.getExtraInterfaces();

        // Add Serializable if settings indicate serializable
        if (settings.isSerializable()) {
            if (interfaces == null) {
                interfaces = new Class<?>[] { java.io.Serializable.class };
            } else {
                // check if already present
                boolean found = false;
                for (Class<?> i : interfaces) {
                    if (i == java.io.Serializable.class) {
                        found = true;
                        break;
                    }
                }
                if (!found) {
                    Class<?>[] newInterfaces = new Class<?>[interfaces.length + 1];
                    System.arraycopy(interfaces, 0, newInterfaces, 0, interfaces.length);
                    newInterfaces[interfaces.length] = java.io.Serializable.class;
                    interfaces = newInterfaces;
                }
            }
        }

        Class<?>[] ancillaryTypes = interfaces == null ? new Class<?>[0] : interfaces;

        Object spiedInstance = settings.getSpiedInstance();
        
        T mock = ClassImposterizer.INSTANCE.imposterise(filter, classToMock, ancillaryTypes);
        
        if (spiedInstance != null) {
            new LenientCopyTool().copyToMock(spiedInstance, mock);
        }
        
        return mock;
    }
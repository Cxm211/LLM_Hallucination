public <T> T createMock(Class<T> classToMock, MockSettingsImpl settings) {
    creationValidator.validateType(classToMock);
    creationValidator.validateExtraInterfaces(classToMock, settings.getExtraInterfaces());
    creationValidator.validateMockedType(classToMock, settings.getSpiedInstance());

    settings.initiateMockName(classToMock);

    MockHandler<T> mockHandler = new MockHandler<T>(settings);
    MethodInterceptorFilter filter = new MethodInterceptorFilter(mockHandler, settings);
    Class<?>[] interfaces = settings.getExtraInterfaces();

    Class<?>[] ancillaryTypes;
    if (settings.isSerializable() && interfaces != null) {
        boolean hasSerializable = false;
        for (Class<?> iface : interfaces) {
            if (iface == java.io.Serializable.class) {
                hasSerializable = true;
                break;
            }
        }
        if (!hasSerializable) {
            Class<?>[] temp = new Class<?>[interfaces.length + 1];
            System.arraycopy(interfaces, 0, temp, 0, interfaces.length);
            temp[interfaces.length] = java.io.Serializable.class;
            ancillaryTypes = temp;
        } else {
            ancillaryTypes = interfaces;
        }
    } else if (settings.isSerializable()) {
        ancillaryTypes = new Class<?>[] { java.io.Serializable.class };
    } else {
        ancillaryTypes = interfaces == null ? new Class<?>[0] : interfaces;
    }

    Object spiedInstance = settings.getSpiedInstance();
    
    T mock = ClassImposterizer.INSTANCE.imposterise(filter, classToMock, ancillaryTypes);
    
    if (spiedInstance != null) {
        new LenientCopyTool().copyToMock(spiedInstance, mock);
    }
    
    return mock;
}
    public <T> T createMock(Class<T> classToMock, MockSettingsImpl settings) {
        creationValidator.validateType(classToMock);
        creationValidator.validateExtraInterfaces(classToMock, settings.getExtraInterfaces());
        creationValidator.validateMockedType(classToMock, settings.getSpiedInstance());

        settings.initiateMockName(classToMock);

        MockHandler<T> mockHandler = new MockHandler<T>(settings);
        MethodInterceptorFilter filter = new MethodInterceptorFilter(mockHandler, settings);
        Class<?>[] interfaces = settings.getExtraInterfaces();
        boolean serializable = settings.isSerializable();
        
        java.util.List<Class<?>> ancillaryList = new java.util.ArrayList<Class<?>>();
        if (interfaces != null) {
            ancillaryList.addAll(java.util.Arrays.asList(interfaces));
        }
        if (serializable && !ancillaryList.contains(java.io.Serializable.class)) {
            ancillaryList.add(java.io.Serializable.class);
        }
        Class<?>[] ancillaryTypes = ancillaryList.toArray(new Class<?>[ancillaryList.size()]);

        Object spiedInstance = settings.getSpiedInstance();
        
        T mock = ClassImposterizer.INSTANCE.imposterise(filter, classToMock, ancillaryTypes);
        
        if (spiedInstance != null) {
            new LenientCopyTool().copyToMock(spiedInstance, mock);
        }
        
        return mock;
    }
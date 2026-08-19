// ===== FIXED org.mockito.internal.creation.MockSettingsImpl :: isSerializable() [lines 75-77] from /Users/grace/Documents/Paper/BugFixing/Interpretation/defects4j_fixed/Mockito/Mockito-17-fixed/src/org/mockito/internal/creation/MockSettingsImpl.java =====
    public boolean isSerializable() {
        return serializable;
    }

// ===== FIXED org.mockito.internal.creation.MockSettingsImpl :: serializable() [lines 22-25] from /Users/grace/Documents/Paper/BugFixing/Interpretation/defects4j_fixed/Mockito/Mockito-17-fixed/src/org/mockito/internal/creation/MockSettingsImpl.java =====
    public MockSettings serializable() {
        this.serializable = true;
        return this;
    }

// ===== FIXED org.mockito.internal.util.MockUtil :: createMock(Class, MockSettingsImpl) [lines 34-61] from /Users/grace/Documents/Paper/BugFixing/Interpretation/defects4j_fixed/Mockito/Mockito-17-fixed/src/org/mockito/internal/util/MockUtil.java =====
    public <T> T createMock(Class<T> classToMock, MockSettingsImpl settings) {
        creationValidator.validateType(classToMock);
        creationValidator.validateExtraInterfaces(classToMock, settings.getExtraInterfaces());
        creationValidator.validateMockedType(classToMock, settings.getSpiedInstance());

        settings.initiateMockName(classToMock);

        MockHandler<T> mockHandler = new MockHandler<T>(settings);
        MethodInterceptorFilter filter = new MethodInterceptorFilter(mockHandler, settings);
        Class<?>[] interfaces = settings.getExtraInterfaces();

        Class<?>[] ancillaryTypes;
        if (settings.isSerializable()) {
            ancillaryTypes = interfaces == null ? new Class<?>[] {Serializable.class} : new ArrayUtils().concat(interfaces, Serializable.class);
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

    public static <T> T mock(Class<T> classToMock, MockSettings mockSettings) {
        mockingProgress.validateState();
        mockingProgress.resetOngoingStubbing();
        return MOCKITO_CORE.mock(classToMock, mockSettings);
    }
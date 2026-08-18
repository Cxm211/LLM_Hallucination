public <T> T mock(Class<T> classToMock, MockSettings mockSettings, boolean shouldResetOngoingStubbing) {
    if (shouldResetOngoingStubbing) {
        mockingProgress.resetOngoingStubbing();
    }
    mockingProgress.validateState();
    return mockUtil.createMock(classToMock, (MockSettingsImpl) mockSettings);
}
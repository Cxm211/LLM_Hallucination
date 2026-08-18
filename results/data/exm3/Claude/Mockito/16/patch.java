public <T> T mock(Class<T> classToMock, MockSettings mockSettings, boolean shouldResetOngoingStubbing) {
    if (shouldResetOngoingStubbing) {
        return mock(classToMock, mockSettings);
    } else {
        mockingProgress.validateState();
        return mockUtil.createMock(classToMock, (MockSettingsImpl) mockSettings);
    }
}
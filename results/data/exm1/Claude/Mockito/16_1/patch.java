public <T> T mock(Class<T> classToMock, MockSettings mockSettings, boolean shouldResetOngoingStubbing) {
    if (shouldResetOngoingStubbing) {
        mockingProgress.resetOngoingStubbing();
    }
    return mock(classToMock, mockSettings);
}
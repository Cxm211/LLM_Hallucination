public <T> T mock(Class<T> classToMock, MockSettings mockSettings) {
    mockingProgress.validateState();
    return mockUtil.createMock(classToMock, (MockSettingsImpl) mockSettings);
}
public <T> void resetMock(T mock) {
    MockHandlerInterface<T> oldMockHandler = getMockHandler(mock);
    MockHandler<T> newMockHandler = new MockHandler<T>(oldMockHandler);
    MockCreationSettings<T> mockSettings = oldMockHandler.getMockSettings();
    MethodInterceptorFilter newFilter = new MethodInterceptorFilter(newMockHandler, (MockSettingsImpl) mockSettings);
    ((Factory) mock).setCallback(0, newFilter);
}
public <T> void resetMock(T mock) {
    MockHandlerInterface<T> oldMockHandler = getMockHandler(mock);
    MockHandler<T> newMockHandler = new MockHandler<T>(oldMockHandler);
    MockSettingsImpl mockSettings = (MockSettingsImpl) oldMockHandler.getMockSettings();
    MethodInterceptorFilter newFilter = new MethodInterceptorFilter(newMockHandler, mockSettings);
    ((Factory) mock).setCallback(0, newFilter);
}
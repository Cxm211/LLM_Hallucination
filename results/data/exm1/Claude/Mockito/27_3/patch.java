public <T> void resetMock(T mock) {
    MockHandlerInterface<T> oldMockHandler = getMockHandler(mock);
    MockHandler<T> newMockHandler = new MockHandler<T>(oldMockHandler);
    MockSettingsImpl settings = (MockSettingsImpl) org.mockito.Mockito.withSettings();
    if (oldMockHandler instanceof MockHandler) {
        MockHandler<T> oldHandler = (MockHandler<T>) oldMockHandler;
        settings.defaultAnswer(oldHandler.getMockSettings().getDefaultAnswer());
    } else {
        settings.defaultAnswer(org.mockito.Mockito.RETURNS_DEFAULTS);
    }
    MethodInterceptorFilter newFilter = new MethodInterceptorFilter(newMockHandler, settings);
    ((Factory) mock).setCallback(0, newFilter);
}
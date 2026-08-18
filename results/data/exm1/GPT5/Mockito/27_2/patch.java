public <T> void resetMock(T mock) {
        MockHandlerInterface<T> oldMockHandler = getMockHandler(mock);
        MockHandler<T> newMockHandler = new MockHandler<T>(oldMockHandler);
        // Preserve original mock settings (including custom default answers, listeners, etc.)
        MockSettingsImpl originalSettings = (MockSettingsImpl) oldMockHandler.getMockSettings();
        MethodInterceptorFilter newFilter = new MethodInterceptorFilter(newMockHandler, originalSettings);
        ((Factory) mock).setCallback(0, newFilter);
    }
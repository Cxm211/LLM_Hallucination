public <T> void resetMock(T mock) {
        MockHandlerInterface<T> oldMockHandler = getMockHandler(mock);
        MockHandler<T> newMockHandler = new MockHandler<T>(oldMockHandler);
        MethodInterceptorFilter newFilter = new MethodInterceptorFilter(newMockHandler, (MockSettingsImpl) oldMockHandler.getMockSettings());
        ((Factory) mock).setCallback(0, newFilter);
    }
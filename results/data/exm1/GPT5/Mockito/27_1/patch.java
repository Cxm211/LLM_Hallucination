public <T> void resetMock(T mock) {
        MockHandlerInterface<T> oldMockHandler = getMockHandler(mock);
        MockHandler<T> newMockHandler = new MockHandler<T>(oldMockHandler);
        // Preserve original mock settings (default answer, listeners, name, etc.)
        MethodInterceptorFilter newFilter = new MethodInterceptorFilter(newMockHandler, newMockHandler.getMockSettings());
        ((Factory) mock).setCallback(0, newFilter);
    }
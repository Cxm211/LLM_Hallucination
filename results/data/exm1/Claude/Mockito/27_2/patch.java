public <T> void resetMock(T mock) {
    MockHandlerInterface<T> oldMockHandler = getMockHandler(mock);
    MockHandler<T> newMockHandler = new MockHandler<T>(oldMockHandler);
    MockSettingsImpl newSettings = (MockSettingsImpl) org.mockito.Mockito.withSettings().defaultAnswer(org.mockito.Mockito.RETURNS_DEFAULTS);
    if (oldMockHandler instanceof MockHandler) {
        MockSettingsImpl oldSettings = ((MockHandler<T>) oldMockHandler).getMockSettings();
        if (oldSettings != null) {
            newSettings = oldSettings.serializable();
            if (oldSettings.isVerboseLoggingEnabled()) {
                newSettings.verboseLogging();
            }
            if (oldSettings.getInvocationListeners() != null && !oldSettings.getInvocationListeners().isEmpty()) {
                newSettings.invocationListeners(oldSettings.getInvocationListeners().toArray(new org.mockito.listeners.InvocationListener[0]));
            }
            if (oldSettings.getDefaultAnswer() != null) {
                newSettings.defaultAnswer(oldSettings.getDefaultAnswer());
            }
        }
    }
    MethodInterceptorFilter newFilter = new MethodInterceptorFilter(newMockHandler, newSettings);
    ((Factory) mock).setCallback(0, newFilter);
}
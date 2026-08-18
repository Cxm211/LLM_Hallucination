public <T> void resetMock(T mock) {
        MockHandlerInterface<T> oldMockHandler = getMockHandler(mock);
        MockHandler<T> newMockHandler = new MockHandler<T>(oldMockHandler);
        if (mock instanceof net.sf.cglib.proxy.Factory) {
            MethodInterceptorFilter newFilter = new MethodInterceptorFilter(newMockHandler, (MockSettingsImpl) org.mockito.Mockito.withSettings().defaultAnswer(org.mockito.Mockito.RETURNS_DEFAULTS));
            ((net.sf.cglib.proxy.Factory) mock).setCallback(0, newFilter);
        } else {
            try {
                java.lang.reflect.InvocationHandler handler = java.lang.reflect.Proxy.getInvocationHandler(mock);
                java.lang.reflect.Field targetField = null;
                for (java.lang.reflect.Field f : handler.getClass().getDeclaredFields()) {
                    if (MockHandlerInterface.class.isAssignableFrom(f.getType())) {
                        targetField = f;
                        break;
                    }
                }
                if (targetField != null) {
                    targetField.setAccessible(true);
                    targetField.set(handler, newMockHandler);
                }
            } catch (Throwable ignored) {
                // Fallback: if not a proxy or cannot adjust handler, do nothing
            }
        }
    }
// ===== FIXED org.mockito.internal.util.MockUtil :: resetMock(T) [lines 62-66] from /Users/grace/Documents/Paper/BugFixing/Interpretation/defects4j_fixed/Mockito/Mockito-27-fixed/src/org/mockito/internal/util/MockUtil.java =====
    public <T> void resetMock(T mock) {
        MockHandlerInterface<T> oldMockHandler = getMockHandler(mock);
        MethodInterceptorFilter newFilter = newMethodInterceptorFilter(oldMockHandler.getMockSettings());
        ((Factory) mock).setCallback(0, newFilter);
    }

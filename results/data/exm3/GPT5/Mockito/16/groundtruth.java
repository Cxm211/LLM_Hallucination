// ===== FIXED org.mockito.Mockito :: mock(Class, MockSettings) [lines 826-828] from /Users/grace/Documents/Paper/BugFixing/Interpretation/defects4j_fixed/Mockito/Mockito-16-fixed/src/org/mockito/Mockito.java =====
    public static <T> T mock(Class<T> classToMock, MockSettings mockSettings) {
        return MOCKITO_CORE.mock(classToMock, mockSettings, true);
    }

// ===== FIXED org.mockito.Mockito :: spy(T) [lines 896-900] from /Users/grace/Documents/Paper/BugFixing/Interpretation/defects4j_fixed/Mockito/Mockito-16-fixed/src/org/mockito/Mockito.java =====
    public static <T> T spy(T object) {
        return MOCKITO_CORE.mock((Class<T>) object.getClass(), withSettings()
                .spiedInstance(object)
                .defaultAnswer(CALLS_REAL_METHODS), true); 
    }

// ===== FIXED org.mockito.internal.MockitoCore :: mock(Class, MockSettings) [lines 32-38] from /Users/grace/Documents/Paper/BugFixing/Interpretation/defects4j_fixed/Mockito/Mockito-16-fixed/src/org/mockito/internal/MockitoCore.java =====
    public <T> T mock(Class<T> classToMock, MockSettings mockSettings, boolean shouldResetOngoingStubbing) {
        mockingProgress.validateState();
        if (shouldResetOngoingStubbing) {
            mockingProgress.resetOngoingStubbing();
        }
        return mockUtil.createMock(classToMock, (MockSettingsImpl) mockSettings);
    }

// ===== FIXED org.mockito.internal.MockitoCore :: mock(Class, MockSettings, boolean) [lines 32-38] from /Users/grace/Documents/Paper/BugFixing/Interpretation/defects4j_fixed/Mockito/Mockito-16-fixed/src/org/mockito/internal/MockitoCore.java =====
    public <T> T mock(Class<T> classToMock, MockSettings mockSettings, boolean shouldResetOngoingStubbing) {
        mockingProgress.validateState();
        if (shouldResetOngoingStubbing) {
            mockingProgress.resetOngoingStubbing();
        }
        return mockUtil.createMock(classToMock, (MockSettingsImpl) mockSettings);
    }

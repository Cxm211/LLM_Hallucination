// buggy function
    public <T> T mock(Class<T> classToMock, MockSettings mockSettings, boolean shouldResetOngoingStubbing) { return mock(classToMock, mockSettings); }

    public <T> T mock(Class<T> classToMock, MockSettings mockSettings) {
        mockingProgress.validateState();
            mockingProgress.resetOngoingStubbing();
        return mockUtil.createMock(classToMock, (MockSettingsImpl) mockSettings);
    }

    public static <T> T mock(Class<T> classToMock, MockSettings mockSettings) {
        return MOCKITO_CORE.mock(classToMock, mockSettings);
    }

    public static <T> T spy(T object) {
        return MOCKITO_CORE.mock((Class<T>) object.getClass(), withSettings()
                .spiedInstance(object)
                .defaultAnswer(CALLS_REAL_METHODS)); 
    }

// trigger testcase
// org/mockitousage/bugs/StubbingMocksThatAreConfiguredToReturnMocksTest.java::shouldAllowStubbingMocksConfiguredWithRETURNS_MOCKS
@Test
    public void shouldAllowStubbingMocksConfiguredWithRETURNS_MOCKS() {
        IMethods mock = mock(IMethods.class, RETURNS_MOCKS);
        when(mock.objectReturningMethodNoArgs()).thenReturn(null);
    }

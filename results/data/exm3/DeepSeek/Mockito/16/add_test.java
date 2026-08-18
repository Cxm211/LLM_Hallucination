// org/mockitousage/bugs/StubbingMocksThatAreConfiguredToReturnMocksTest.java
@Test
    public void shouldRespectShouldResetOngoingStubbingFlag() throws Exception {
        // Start ongoing stubbing without completing it
        List mock1 = mock(List.class);
        OngoingStubbing<Integer> ongoing = when(mock1.size());
        
        // Get internal MockitoCore instance via reflection
        Field field = Mockito.class.getDeclaredField("MOCKITO_CORE");
        field.setAccessible(true);
        Object mockitoCore = field.get(null);
        
        // Get the three-parameter mock method
        Method mockMethod = mockitoCore.getClass().getDeclaredMethod("mock", Class.class, MockSettings.class, boolean.class);
        
        // Create another mock with shouldResetOngoingStubbing = false
        MockSettings settings = withSettings().defaultAnswer(RETURNS_DEFAULTS);
        List mock2 = (List) mockMethod.invoke(mockitoCore, List.class, settings, false);
        
        // Continue the ongoing stubbing; if reset happened, this will throw
        ongoing.thenReturn(42);
        
        // Verify the stubbing took effect
        assertEquals(42, mock1.size());
    }

// org/mockitousage/bugs/VerifyingWithAnExtraCallToADifferentMockTest.java::shouldAllowVerifyingWhenOtherMockCallIsInTheSameLine
@Test 
    public void shouldDetectTooFewInvocationsWhenOtherMockCallInSameLine() {
        when(mock.otherMethod()).thenReturn("foo");

        mockTwo.simpleMethod("foo");

        try {
            verify(mockTwo, times(2)).simpleMethod(mock.otherMethod());
            fail();
        } catch (org.mockito.exceptions.base.MockitoAssertionError e) {
            // expected
        }
    }
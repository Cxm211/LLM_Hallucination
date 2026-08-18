// org/mockitousage/bugs/VerifyingWithAnExtraCallToADifferentMockTest.java
@Test 
    public void shouldAllowVerifyingWhenOtherMockCallReturnsDefaultValue() {
        // when - no stubbing for other mock
        mockTwo.simpleMethod(null);
        // then - extra call to different mock in same line should not consume verification and should pass
        verify(mockTwo).simpleMethod(mock.otherMethod());
    }
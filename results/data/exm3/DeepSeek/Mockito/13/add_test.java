// org/mockitousage/bugs/VerifyingWithAnExtraCallToADifferentMockTest.java
@Test 
    public void shouldAllowVerifyingWhenOtherMockCallIsInTheSameLineWithDefaultAnswer() {
        //given
        // no stub for mock.otherMethod(), default answer will return null
        //when
        mockTwo.simpleMethod(null);
        //then
        verify(mockTwo).simpleMethod(mock.otherMethod());
    }

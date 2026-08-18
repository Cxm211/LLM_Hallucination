// org/mockitousage/bugs/VerifyingWithAnExtraCallToADifferentMockTest.java
@Test 
    public void shouldAllowVerifyingWithTimesAndOtherMockCall() {
        //given
        when(mock.otherMethod()).thenReturn("foo");
        //when
        mockTwo.simpleMethod("foo");
        mockTwo.simpleMethod("foo");
        //then
        verify(mockTwo, times(2)).simpleMethod(mock.otherMethod());
    }

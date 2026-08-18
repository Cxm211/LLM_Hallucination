// org/mockitousage/bugs/VerifyingWithAnExtraCallToADifferentMockTest.java
@Test
    public void shouldAllowVerifyingWithTimesWhenOtherMockCallIsInTheSameLine() {
        //given
        when(mock.otherMethod()).thenReturn("foo");
        
        //when
        mockTwo.simpleMethod("foo");
        
        //then
        verify(mockTwo, times(1)).simpleMethod(mock.otherMethod());
    }
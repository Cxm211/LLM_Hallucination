// org/mockitousage/bugs/VerifyingWithAnExtraCallToADifferentMockTest.java
@Test 
    public void shouldAllowVerifyingWithNeverWhenOtherMockCallIsInTheSameLine() {
        //given
        when(mock.otherMethod()).thenReturn("foo");
        
        //when
        // do not call mockTwo.simpleMethod
        
        //then
        verify(mockTwo, never()).simpleMethod(mock.otherMethod());
    }

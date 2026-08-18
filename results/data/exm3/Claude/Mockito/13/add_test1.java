// org/mockitousage/bugs/VerifyingWithAnExtraCallToADifferentMockTest.java
@Test
    public void shouldAllowVerifyingWithAtLeastWhenOtherMockCallIsInTheSameLine() {
        //given
        when(mock.otherMethod()).thenReturn("bar");
        
        //when
        mockTwo.simpleMethod("bar");
        mockTwo.simpleMethod("bar");
        
        //then
        verify(mockTwo, atLeast(1)).simpleMethod(mock.otherMethod());
    }
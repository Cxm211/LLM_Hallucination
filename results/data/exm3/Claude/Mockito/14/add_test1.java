// org/mockitousage/bugs/VerifyingWithAnExtraCallToADifferentMockTest.java
@Test
public void shouldAllowVerifyingWithNestedMockCalls() {
    //given
    when(mock.otherMethod()).thenReturn("inner");
    when(mock.simpleMethod(anyString())).thenReturn("outer");
    
    //when
    mockTwo.simpleMethod("result");
    
    //then
    verify(mockTwo).simpleMethod(mock.simpleMethod(mock.otherMethod()));
}
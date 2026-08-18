// org/mockitousage/bugs/VerifyingWithAnExtraCallToADifferentMockTest.java
@Test
public void shouldAllowVerifyingWithMultipleMockCallsInArguments() {
    //given
    when(mock.otherMethod()).thenReturn("foo");
    when(mock.simpleMethod(anyString())).thenReturn("bar");
    
    //when
    mockTwo.simpleMethod("foobar");
    
    //then
    verify(mockTwo).simpleMethod(mock.otherMethod() + mock.simpleMethod("test"));
}
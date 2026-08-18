// org/mockitousage/bugs/StubbingMocksThatAreConfiguredToReturnMocksTest.java
@Test
public void shouldResetOngoingStubbingForRegularMock() {
    IMethods mock = mock(IMethods.class);
    when(mock.simpleMethod()).thenReturn("result");
    assertEquals("result", mock.simpleMethod());
}
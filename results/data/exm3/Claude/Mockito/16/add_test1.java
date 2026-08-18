// org/mockitousage/bugs/StubbingMocksThatAreConfiguredToReturnMocksTest.java
@Test
public void shouldAllowMultipleStubbingsWithRETURNS_MOCKS() {
    IMethods mock = mock(IMethods.class, RETURNS_MOCKS);
    when(mock.objectReturningMethodNoArgs()).thenReturn("first");
    when(mock.simpleMethod()).thenReturn("second");
    assertEquals("first", mock.objectReturningMethodNoArgs());
    assertEquals("second", mock.simpleMethod());
}
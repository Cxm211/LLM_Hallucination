// org/mockitousage/bugs/StubbingMocksThatAreConfiguredToReturnMocksTest.java
@Test
public void shouldAllowNestedStubbingWithRETURNS_MOCKS() {
    IMethods mock = mock(IMethods.class, RETURNS_MOCKS);
    IMethods innerMock = mock.iMethodsReturningMethod();
    when(innerMock.objectReturningMethodNoArgs()).thenReturn("test");
    assertEquals("test", innerMock.objectReturningMethodNoArgs());
}
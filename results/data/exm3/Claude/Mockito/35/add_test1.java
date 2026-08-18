// org/mockitousage/bugs/NPEWithCertainMatchersTest.java
@Test
public void shouldNotThrowNPEWhenBooleanPassedToEq() {
    mock.booleanArgumentMethod(true);
    
    verify(mock).booleanArgumentMethod(eq(Boolean.TRUE));
}
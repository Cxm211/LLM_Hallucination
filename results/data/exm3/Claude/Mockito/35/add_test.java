// org/mockitousage/bugs/NPEWithCertainMatchersTest.java
@Test
public void shouldNotThrowNPEWhenLongPassed() {
    mock.longArgumentMethod(200L);
    
    verify(mock).longArgumentMethod(isA(Long.class));
}
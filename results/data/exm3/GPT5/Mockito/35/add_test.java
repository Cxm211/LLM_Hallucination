// org/mockitousage/bugs/NPEWithCertainMatchersTest.java
@Test
    public void shouldNotThrowNPEWhenBooleanPassed() {
        mock.booleanArgumentMethod(true);
        verify(mock).booleanArgumentMethod(isA(Boolean.class));
    }
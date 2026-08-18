// org/mockito/internal/creation/DelegatingMethodTest.java
@Test
public void equals_should_return_false_when_null() throws Exception {
    assertFalse(delegatingMethod.equals(null));
}
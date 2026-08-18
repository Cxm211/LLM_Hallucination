// org/mockito/internal/creation/DelegatingMethodTest.java
@Test
public void equals_should_return_false_when_different_type() throws Exception {
    assertFalse(delegatingMethod.equals("not a DelegatingMethod"));
}
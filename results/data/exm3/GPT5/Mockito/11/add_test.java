// org/mockito/internal/creation/DelegatingMethodTest.java::hashcode_should_match_underlying_method
@Test
public void hashcode_should_match_underlying_method() throws Exception {
    assertEquals(someMethod.hashCode(), delegatingMethod.hashCode());
}
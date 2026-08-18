// org/mockito/internal/creation/DelegatingMethodTest.java
@Test
public void hashCode_should_use_method_hashcode() throws Exception {
    assertEquals(someMethod.hashCode(), delegatingMethod.hashCode());
}
// org/mockito/internal/creation/DelegatingMethodTest.java
@Test
public void equals_should_return_false_when_different_method() throws Exception {
    Method differentMethod = getClass().getDeclaredMethod("hashCode_should_use_method_hashcode");
    DelegatingMethod different = new DelegatingMethod(differentMethod);
    assertFalse(delegatingMethod.equals(different));
}
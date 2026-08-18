// com/google/javascript/jscomp/TypeCheckTest.java
public void testFunctionArgumentsNullFunctionType() throws Exception {
  testTypes(
      "function f(a) { return a; }",
      (String) null);
}
// com/google/javascript/jscomp/TypeCheckTest.java
public void testInferredReturnTypeUndefinedForExplicitReturn() throws Exception {
  testTypes(
      "function f() { return; } /** @param {number} x */ function g(x) {}" +
      "g(f());",
      "actual parameter 1 of g does not match formal parameter\n" +
      "found   : undefined\n" +
      "required: number");
}
// com/google/javascript/jscomp/TypeCheckTest.java
public void testFunctionArgumentsNoJSDoc() throws Exception {
  testTypes(
      "function f(x, y) { var z = x + y; return z; }",
      (String) null);
}
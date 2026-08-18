// com/google/javascript/jscomp/LooseTypeCheckTest.java
public void testFunctionArgumentsNoJSDoc() throws Exception {
  testTypes(
      "function f(x, y) { var z = x + y; return z; }",
      (String) null);
}
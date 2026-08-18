// com/google/javascript/jscomp/InlineFunctionsTest.java
public void testInlineFunctions31_nested() {
  // Test nested labels with same name in function and outer scope
  test("function foo(){ outer:{inner:{5;}} }" +
      "outer:{foo();}",
      "outer:{{JSCompiler_inline_label_outer_0:{JSCompiler_inline_label_inner_1:{5}}}}");
}
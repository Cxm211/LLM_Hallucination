// com/google/javascript/jscomp/InlineFunctionsTest.java
public void testInlineFunctions31_multiple() {
  // Test multiple labels in function with outer scope conflicts
  test("function foo(){ a:{1;} b:{2;} }" +
      "a:{b:{foo();}}",
      "a:{b:{{JSCompiler_inline_label_a_0:{1} JSCompiler_inline_label_b_1:{2}}}}");
}
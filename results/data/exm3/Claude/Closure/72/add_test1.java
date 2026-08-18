// com/google/javascript/jscomp/InlineFunctionsTest.java
public void testInlineFunctions31_unreferenced() {
  // Test unreferenced label in function doesn't conflict with outer label
  test("function foo(){ lab:{break lab;} }" +
      "lab:{foo();}",
      "lab:{{JSCompiler_inline_label_0:{break JSCompiler_inline_label_0}}}");
}
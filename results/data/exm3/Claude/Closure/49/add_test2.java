// com/google/javascript/jscomp/InlineFunctionsTest.java
public void testInlineFunctionWithNoParameters() {
  test("function foo(){var x=5;return x+10;} foo();",
       "{var x$$inline_0=5;x$$inline_0+10}");
}
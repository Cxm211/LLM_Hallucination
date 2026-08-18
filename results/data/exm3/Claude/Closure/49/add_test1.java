// com/google/javascript/jscomp/InlineFunctionsTest.java
public void testInlineFunctionWithMultipleParameters() {
  test("function foo(a, b, c){return a+b+c;} foo(1, 2, 3);",
       "{var a$$inline_0=1;var b$$inline_1=2;var c$$inline_2=3;" +
       "a$$inline_0+b$$inline_1+c$$inline_2}");
}
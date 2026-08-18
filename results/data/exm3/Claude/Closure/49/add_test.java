// com/google/javascript/jscomp/FunctionInjectorTest.java
public void testInlineWithNestedFunction() {
  helperInlineReferenceToFunction(
      "function foo(a){function bar(b){return b+1;} return bar(a);} foo(x())",
      "function foo(a){function bar(b){return b+1;}return bar(a);}" +
      "{var a$$inline_0=x();" +
      "var bar$$inline_1=function(b$$inline_2){return b$$inline_2+1;};" +
      "bar$$inline_1(a$$inline_0)}",
      "foo", INLINE_BLOCK);
}
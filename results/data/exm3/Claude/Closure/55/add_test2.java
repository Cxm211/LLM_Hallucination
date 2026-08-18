// com/google/javascript/jscomp/FunctionRewriterTest.java
public void testRegularFunctionExpressionReduced() {
  test(
      "var f = function() { return 1; }; f(); f(); f(); f(); f(); f();",
      "var f = function $jscomp$1() { return 1; }; $jscomp$1(); $jscomp$1(); $jscomp$1(); $jscomp$1(); $jscomp$1(); $jscomp$1();", 1);
}
// com/google/javascript/jscomp/FunctionInjectorTest.java
public void testInlineFunctionWithRecursiveExpression() {
    helperInlineReferenceToFunction(
        "var f = function fact(n) { return n <= 1 ? 1 : n * fact(n-1); }; f(5);",
        "var f = function fact(n) { return n <= 1 ? 1 : n * fact(n-1); }; " +
        "{var n$$inline_0=5; n$$inline_0<=1?1:n$$inline_0*fact(n$$inline_0-1)}",
        "f", INLINE_BLOCK);
  }

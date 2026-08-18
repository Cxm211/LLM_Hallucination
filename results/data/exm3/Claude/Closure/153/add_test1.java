// com/google/javascript/jscomp/NormalizeTest.java
public void testDuplicateFunctionDeclaration() {
    test("function f(){}", "/** @suppress {duplicate} */ function f(){var a;}", "function f(){}f=function(){var a;}", null, null);
  }
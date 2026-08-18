// com/google/javascript/jscomp/NormalizeTest.java
public void testDuplicateFunctionInExternsNoSuppression() {
    test("function f() {}", "function f() { return 1; }", "function f() { return 1; }", null, null);
  }

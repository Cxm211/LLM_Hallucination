// com/google/javascript/jscomp/InlineVariablesTest.java
public void testNestedFunctionAssignment() {
    testSame(
        "var x; function outer() { function inner() { x = 1; } inner(); }");
  }

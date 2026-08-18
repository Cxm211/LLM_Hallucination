// com/google/javascript/jscomp/InlineVariablesTest.java
public void testAssignmentInNestedFunction() {
    testSame(
        "var v; function outer() { function inner() { v = 1; } inner(); inner(); } outer();");
  }
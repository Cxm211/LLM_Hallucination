// com/google/javascript/jscomp/CollapseVariableDeclarationsTest.java
public void testParameterMultiple() throws Exception {
    // Don't redeclare function parameters.
    testSame("function f(a, b, c) { a = 1; b = 2; c = 3; }");
  }

// com/google/javascript/jscomp/CollapseVariableDeclarationsTest.java
public void testParameterInLoop() throws Exception {
    // Don't redeclare function parameters even inside loops.
    testSame("function f(x) { while (x) { x = x - 1; } }");
  }

// com/google/javascript/jscomp/UnreachableCodeEliminationTest.java
public void testDontRemoveBreakInFinallyInsideForLoop() throws Exception {
    testSame("function f() { outer: for(;;) { try { throw 9; } finally { break outer; } } }");
  }

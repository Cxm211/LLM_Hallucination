// com/google/javascript/jscomp/UnreachableCodeEliminationTest.java
public void testDontRemoveContinueInFinallyInsideWhile() throws Exception {
    testSame("function f() { outer: while(true) { try { throw 9; } finally { continue outer; } } }");
  }

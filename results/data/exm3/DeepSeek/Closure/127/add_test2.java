// com/google/javascript/jscomp/UnreachableCodeEliminationTest.java
public void testDontRemoveReturnInFinallyInsideIf() throws Exception {
    testSame("function f() { if (true) { try { throw 9; } finally { return; } } }");
  }

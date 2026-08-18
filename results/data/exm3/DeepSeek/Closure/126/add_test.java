// com/google/javascript/jscomp/MinimizeExitPointsTest.java
public void testDontRemoveLabeledBreakInFinallyWithLoop() throws Exception {
    foldSame("function f() { outer: while (true) { try { a(); } finally { break outer; } } }");
  }

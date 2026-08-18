// com/google/javascript/jscomp/MinimizeExitPointsTest.java
public void testDontRemoveLabeledContinueInFinally() throws Exception {
    foldSame("function f() { outer: for (;;) { try { b(); } finally { continue outer; } } }");
  }

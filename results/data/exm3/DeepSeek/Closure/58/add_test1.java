// com/google/javascript/jscomp/LiveVariableAnalysisTest.java
public void testSimpleVarInForIn() {
  // x is only written in for-in, so should not be live before if no reads
  assertNotLiveBeforeX("var x; X:for (x in foo) { }", "x");
}

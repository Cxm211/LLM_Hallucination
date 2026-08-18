// com/google/javascript/jscomp/LiveVariableAnalysisTest.java
public void testArrayIndexVarInForIn() {
  assertLiveBeforeX("var a = []; var i = 0; X:for (a[i] in foo) { }", "a");
  assertLiveBeforeX("var a = []; var i = 0; X:for (a[i] in foo) { }", "i");
}

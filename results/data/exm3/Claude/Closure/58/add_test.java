// com/google/javascript/jscomp/LiveVariableAnalysisTest.java
public void testComplexExpressionInForIn() {
    assertLiveBeforeX("var a = [0]; var b = {}; X:for (a[b.prop] in foo) { }", "a", "b");
  }
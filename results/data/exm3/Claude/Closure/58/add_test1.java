// com/google/javascript/jscomp/LiveVariableAnalysisTest.java
public void testPropertyAccessInForIn() {
    assertLiveBeforeX("var obj = {}; X:for (obj.prop in foo) { }", "obj");
  }
// com/google/javascript/jscomp/LiveVariableAnalysisTest.java::testForInVarLhs
public void testForInVarLhs() { assertNotLiveBeforeX("X:for (var x in y) {}", "x"); }
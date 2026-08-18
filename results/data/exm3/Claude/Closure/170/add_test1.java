// com/google/javascript/jscomp/FlowSensitiveInlineVariablesTest.java
public void testVarAssignInComplexChain() {
  noInline("var i = 0; return ((i = 5), (i = 10)), i;");
  noInline("var i = 0; return (i = 5), (i = 10), i;");
}
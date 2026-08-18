// com/google/javascript/jscomp/FlowSensitiveInlineVariablesTest.java
public void testVarAssignInBinaryOps() {
  noInline("var i = 0; return (i = 5) - i;");
  noInline("var i = 0; return (i = 5) / i;");
  noInline("var i = 0; return (i = 5) % i;");
}
// com/google/javascript/jscomp/FlowSensitiveInlineVariablesTest.java
public void testVarAssignInsideNestedExpression() {
  noInline("var i = 0; return (i = 5) + (i = 10) + i;");
  noInline("var i = 0; var j = (i = 5) * i; return j;");
}
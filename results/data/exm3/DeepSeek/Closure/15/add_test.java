// com/google/javascript/jscomp/FlowSensitiveInlineVariablesTest.java
public void testDeletePropertyDot() {
  noInline("var a,b,x = a in b; delete a.b; x");
}

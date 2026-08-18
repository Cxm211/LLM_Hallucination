// com/google/javascript/jscomp/FlowSensitiveInlineVariablesTest.java
public void testDeleteMultiple() {
  noInline("var a,b,x = a in b; delete a[b]; delete a.c; x");
}

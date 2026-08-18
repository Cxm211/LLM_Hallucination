// com/google/javascript/jscomp/FlowSensitiveInlineVariablesTest.java
public void testDeleteWithMultipleReferences() {
    noInline("var a = 1; var b = a; delete obj[b]; print(a);");
  }
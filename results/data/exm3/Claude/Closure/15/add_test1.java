// com/google/javascript/jscomp/FlowSensitiveInlineVariablesTest.java
public void testDeleteBeforeUse() {
    noInline("var x = getProperty(); delete obj[x]; use(x);");
  }
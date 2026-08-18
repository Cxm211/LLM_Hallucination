// com/google/javascript/jscomp/FlowSensitiveInlineVariablesTest.java
public void testDeleteInConditional() {
    noInline("var x = key; if (cond) { delete obj[x]; } print(x);");
  }
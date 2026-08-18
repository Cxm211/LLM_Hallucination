// com/google/javascript/jscomp/FlowSensitiveInlineVariablesTest.java
public void testVarAssignInsideNestedHook() {
    noInline("var i = 0; return 1 ? (2 ? (i = 5) : 0) : 0, i;");
  }

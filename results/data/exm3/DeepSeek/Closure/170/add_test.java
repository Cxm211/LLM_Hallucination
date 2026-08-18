// com/google/javascript/jscomp/FlowSensitiveInlineVariablesTest.java
public void testVarAssignInsideHookWithReadInsideHook() {
    noInline("var i = 0; return 1 ? (i = 5, i) : 0;");
  }

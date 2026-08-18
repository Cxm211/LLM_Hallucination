// com/google/javascript/jscomp/FlowSensitiveInlineVariablesTest.java
public void testUseBeforeDefinitionNoCrash() {
    // x is used before definition on some path, should not crash.
    noInline("var x; if (false) { x = 1; } print(x);");
  }

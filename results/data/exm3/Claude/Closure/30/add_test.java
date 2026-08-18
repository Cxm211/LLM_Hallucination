// com/google/javascript/jscomp/FlowSensitiveInlineVariablesTest.java
public void testDependsOnOuterScopeVarsWithNullDef() {
  inline(
      "function f() { var x = unknown; return x; }",
      "function f() { var x; return unknown; }");
}
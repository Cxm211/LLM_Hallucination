// com/google/javascript/jscomp/FlowSensitiveInlineVariablesTest.java
public void testInlineWithUndeclaredVariable() {
  inline(
      "var x = undeclaredVar; print(x);",
      "var x; print(undeclaredVar);");
}
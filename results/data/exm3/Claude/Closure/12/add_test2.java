// com/google/javascript/jscomp/FlowSensitiveInlineVariablesTest.java
public void testTryWithoutCatch() {
  inline(
      "var x = 1; " +
      "try { x = x + 2; } finally {}" +
      "return x;",
      "try { var x = 1 + 2; } finally {}" +
      "return x;");
}
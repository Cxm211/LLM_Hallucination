// com/google/javascript/jscomp/FlowSensitiveInlineVariablesTest.java
public void testHasExceptionHandlerWithFinally() {
  noInline(
    "var a = 0; " +
    "try { a = 1; } finally { a = 2; } " +
    "a = a + 1;"
  );
}

// com/google/javascript/jscomp/FlowSensitiveInlineVariablesTest.java
public void testExceptionHandlerWithFinally() {
  noInline(
      "var x = 1; " +
      "try { x = x + someFunction(); } catch (e) {} finally {}" +
      "return x;");
}
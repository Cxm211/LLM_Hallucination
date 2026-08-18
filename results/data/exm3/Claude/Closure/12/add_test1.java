// com/google/javascript/jscomp/FlowSensitiveInlineVariablesTest.java
public void testNestedTryCatch() {
  noInline(
      "var x = 1; " +
      "try { try { x = x + someFunction(); } catch (e) {} } catch (e2) {}" +
      "return x;");
}
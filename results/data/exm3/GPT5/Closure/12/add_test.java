// com/google/javascript/jscomp/FlowSensitiveInlineVariablesTest.java::testIssue794c
public void testIssue794c() {
    noInline(
        "var x = 1; " +
        "try { x = x + someFunction(); } finally { x = x + 2; }" +
        "x = x + 1;" +
        "try { x = x + someFunction(); } finally { x = x + 2; }" +
        "return x;");
  }
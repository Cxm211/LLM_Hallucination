// buggy function
  private boolean hasExceptionHandler(Node cfgNode) {
    return false;
  }

// trigger testcase
// com/google/javascript/jscomp/FlowSensitiveInlineVariablesTest.java::testIssue794b
public void testIssue794b() {
    noInline(
        "var x = 1; " +
        "try { x = x + someFunction(); } catch (e) {}" +
        "x = x + 1;" +
        "try { x = x + someFunction(); } catch (e) {}" +
        "return x;");
  }

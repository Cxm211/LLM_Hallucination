// com/google/javascript/jscomp/CheckMissingReturnTest.java
public void testIssue779_nested_catch_finally() {
    testNotMissing(
        "var a = f(); try { try { if (a > 0) return 1; } catch(e) { return 2; } } finally { a = 5; } return 3;");
  }
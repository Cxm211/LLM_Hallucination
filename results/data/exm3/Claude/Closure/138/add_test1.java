// com/google/javascript/jscomp/TypeCheckTest.java
public void testIssue124c() throws Exception {
    testTypes(
        "var t = null;" +
        "function test() {" +
        "  if (t != null) { t = 'string'; }" +
        "  t = 1;" +
        "}");
  }
// com/google/javascript/jscomp/InlineGettersTest.java
public void testIssue2508576_2() {
  // Method defined by an extern that is aliased multiple times should be left alone.
  String externs = "function alert(a) {}";
  testSame(externs, "var x = alert; ({a:x,b:x}).a(\"a\")", null);
}
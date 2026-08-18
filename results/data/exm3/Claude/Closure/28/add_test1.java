// com/google/javascript/jscomp/InlineFunctionsTest.java
public void testIssue728WithLongerIdentifiers() {
  String f = "var funcWithLongerName = function() { return false; };";
  StringBuilder calls = new StringBuilder();
  StringBuilder folded = new StringBuilder();
  for (int i = 0; i < 30; i++) {
    calls.append("if (!funcWithLongerName()) alert('x');");
    folded.append("if (!false) alert('x');");
  }
  test(f + calls, folded.toString());
}
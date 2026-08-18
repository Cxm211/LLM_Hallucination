// com/google/javascript/jscomp/CheckGlobalThisTest.java
public void testIssue182c() {
  testFailure("var NS = {method: function() { var x = this; }};");
}
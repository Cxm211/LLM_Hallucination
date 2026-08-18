// com/google/javascript/jscomp/RemoveUnusedVarsTest.java
public void testIssue618_non_literal_no_alias() {
  this.removeGlobal = false;
  test(
      "function f(x) {\n" +
      "  var b = x;\n" +
      "  return 42;\n" +
      "}",
      "function f(x) {\n" +
      "  return 42;\n" +
      "}");
}
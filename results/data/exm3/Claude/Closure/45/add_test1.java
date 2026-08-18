// com/google/javascript/jscomp/RemoveUnusedVarsTest.java
public void testIssue618_literal_with_property() {
  this.removeGlobal = false;
  test(
      "function f() {\n" +
      "  var b = [];\n" +
      "  b[0] = 1;\n" +
      "  return 42;\n" +
      "}",
      "function f() {\n" +
      "  return 42;\n" +
      "}");
}
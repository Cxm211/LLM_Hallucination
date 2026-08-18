// com/google/javascript/jscomp/RemoveUnusedVarsTest.java
public void testIssue618_aliased_no_property() {
  this.removeGlobal = false;
  testSame(
      "function f() {\n" +
      "  var a = [], b;\n" +
      "  a.push(b = []);\n" +
      "  return a;\n" +
      "}");
}
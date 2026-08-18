// com/google/javascript/jscomp/RemoveUnusedVarsTest.java::testIssue618_1
public void testIssue618_alias() {
    this.removeGlobal = false;
    testSame(
        "function f() {\n" +
        "  var b;\n" +
        "  foo(b = []);\n" +
        "  b[0] = 1;\n" +
        "}");
  }
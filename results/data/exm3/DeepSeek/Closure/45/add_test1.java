// com/google/javascript/jscomp/RemoveUnusedVarsTest.java
public void testIssue618_3() {
    this.removeGlobal = false;
    testSame(
        "function f() {\n" +
        "  var b;\n" +
        "  b = [];\n" +
        "  b[0] = 1;\n" +
        "}");
  }

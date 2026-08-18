// com/google/javascript/jscomp/RemoveUnusedVarsTest.java
public void testIssue618_2() {
    this.removeGlobal = false;
    testSame(
        "function f() {\n" +
        "  var b = {};\n" +
        "  b.x = 1;\n" +
        "}");
  }

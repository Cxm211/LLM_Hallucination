// com/google/javascript/jscomp/InlineVariablesTest.java
public void testAliasCandidateWithArgumentsModified() {
    testSame(
        "function g(callback) {\n" +
        "  var f = callback;\n" +
        "  var g = f;\n" +
        "  arguments[0] = this;\n" +
        "  g.apply(this, arguments);\n" +
        "}");
  }

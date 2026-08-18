// com/google/javascript/jscomp/InlineVariablesTest.java
public void testArgumentsModifiedInNestedBlock() {
    testSame(
        "function g(callback) {\n" +
        "  var f = callback;\n" +
        "  if (true) {\n" +
        "    arguments[0] = this;\n" +
        "  }\n" +
        "  f.apply(this, arguments);\n" +
        "}");
  }

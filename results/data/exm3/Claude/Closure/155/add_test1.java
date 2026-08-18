// com/google/javascript/jscomp/InlineVariablesTest.java
public void testArgumentsNotModified() {
  test(
    "function g(callback) {\n" +
    "  var f = callback;\n" +
    "  var x = arguments;\n" +
    "  f.apply(this, arguments);\n" +
    "}",
    "function g(callback) {\n" +
    "  var x = arguments;\n" +
    "  callback.apply(this, arguments);\n" +
    "}");
}
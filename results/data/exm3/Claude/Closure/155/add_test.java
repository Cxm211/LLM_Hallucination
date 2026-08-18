// com/google/javascript/jscomp/InlineVariablesTest.java
public void testArgumentsModifiedInSiblingFunction() {
  test(
    "function g(callback) {\n" +
    "  var f = callback;\n" +
    "  f.apply(this, arguments);\n" +
    "  function inner1(callback) {" +
    "    arguments[0] = this;" +
    "  }" +
    "  function inner2(callback) {" +
    "    var x = callback;\n" +
    "    x.apply(this);\n" +
    "  }" +
    "}",
    "function g(callback) {\n" +
    "  callback.apply(this, arguments);\n" +
    "  function inner1(callback) {" +
    "    arguments[0] = this;" +
    "  }" +
    "  function inner2(callback) {" +
    "    callback.apply(this);\n" +
    "  }" +
    "}");
}
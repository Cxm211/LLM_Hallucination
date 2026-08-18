// com/google/javascript/jscomp/InlineVariablesTest.java::testArgumentsEscapedInInnerFunction
public void testArgumentsEscapedInInnerFunction() {
    test(
      "function g(callback) {\n" +
      "  var f = callback;\n" +
      "  f.apply(this, arguments);\n" +
      "  function inner(callback) {" +
      "    h(arguments,this);\n" +
      "    callback.apply(this);\n" +
      "  }" +
      "}",
      "function g(callback) {\n" +
      "  callback.apply(this, arguments);\n" +
      "  function inner(callback) {" +
      "    h(arguments,this);\n" +
      "    callback.apply(this);\n" +
      "  }" +
      "}");
  }
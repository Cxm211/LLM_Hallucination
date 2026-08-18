// com/google/javascript/jscomp/TypeCheckTest.java
public void testInferTemplateEmptyInference() throws Exception {
    testTypes(
        "/**\n" +
        " * @template T\n" +
        " * @param {function(T)} callback\n" +
        " */\n" +
        "function f(callback) {}\n" +
        "\n" +
        "/** @param {number} x */\n" +
        "function g(x) {}\n" +
        "\n" +
        "f(g);");
  }

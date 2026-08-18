// com/google/javascript/jscomp/TypeCheckTest.java
public void testIssue1047_NestedProperty() throws Exception {
    testTypes(
        "/**\n" +
        " * @constructor\n" +
        " */\n" +
        "function Inner() {}\n" +
        "\n" +
        "/**\n" +
        " * @constructor\n" +
        " */\n" +
        "function Outer() {\n" +
        "  /** @type {Inner} */\n" +
        "  this.inner = new Inner();\n" +
        "}\n" +
        "\n" +
        "/**\n" +
        " * @constructor\n" +
        " */\n" +
        "function Container(outer) {\n" +
        "  /**\n" +
        "   * @type {Outer} \n" +
        "   * @private\n" +
        "   */\n" +
        "  this.outer_;\n" +
        "\n" +
        "  var x = this.outer_.inner.missing;\n" +
        "}",
        "Property missing never defined on Inner");
  }
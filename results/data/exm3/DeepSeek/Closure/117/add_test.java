// com/google/javascript/jscomp/TypeCheckTest.java
public void testIssue1047_chain() throws Exception {
    testTypes(
        "/**\n" +
        " * @constructor\n" +
        " */\n" +
        "function Container() {\n" +
        "  /** @type {Box} */ this.box = new Box();\n" +
        "}\n" +
        "\n" +
        "/**\n" +
        " * @constructor\n" +
        " */\n" +
        "function Box() {\n" +
        "  /** @type {number} */ this.width = 5;\n" +
        "}\n" +
        "\n" +
        "var c = new Container();\n" +
        "var x = c.box.height;\n",
        "Property height never defined on Box");
  }

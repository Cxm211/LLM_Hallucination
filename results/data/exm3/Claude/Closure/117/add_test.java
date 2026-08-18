// com/google/javascript/jscomp/TypeCheckTest.java
public void testIssue1047_InheritedProperty() throws Exception {
    testTypes(
        "/**\n" +
        " * @constructor\n" +
        " */\n" +
        "function Base() {}\n" +
        "\n" +
        "/**\n" +
        " * @type {string}\n" +
        " */\n" +
        "Base.prototype.inheritedProp;\n" +
        "\n" +
        "/**\n" +
        " * @constructor\n" +
        " * @extends {Base}\n" +
        " */\n" +
        "function Derived() {}\n" +
        "\n" +
        "/**\n" +
        " * @constructor\n" +
        " */\n" +
        "function C3(derived) {\n" +
        "  /**\n" +
        "   * @type {Derived} \n" +
        "   * @private\n" +
        "   */\n" +
        "  this.derived_;\n" +
        "\n" +
        "  var x = this.derived_.nonExistentProp;\n" +
        "}",
        "Property nonExistentProp never defined on Derived");
  }
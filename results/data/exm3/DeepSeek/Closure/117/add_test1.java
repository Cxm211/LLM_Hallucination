// com/google/javascript/jscomp/TypeCheckTest.java
public void testIssue1047_interface() throws Exception {
    testTypes(
        "/**\n" +
        " * @interface\n" +
        " */\n" +
        "function Drawable() {}\n" +
        "Drawable.prototype.draw = function() {};\n" +
        "\n" +
        "/**\n" +
        " * @constructor\n" +
        " * @implements {Drawable}\n" +
        " */\n" +
        "function Circle() {}\n" +
        "Circle.prototype.draw = function() {};\n" +
        "\n" +
        "/**\n" +
        " * @constructor\n" +
        " */\n" +
        "function Scene() {\n" +
        "  /** @type {Drawable} */ this.obj = new Circle();\n" +
        "}\n" +
        "\n" +
        "var s = new Scene();\n" +
        "var y = s.obj.rotate;\n",
        "Property rotate never defined on Drawable");
  }

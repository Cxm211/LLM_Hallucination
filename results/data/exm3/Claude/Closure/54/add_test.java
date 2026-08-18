// com/google/javascript/jscomp/TypeCheckTest.java
public void testIssue537c() throws Exception {
    testTypes(
        "/** @constructor */ function Foo() {}" +
        "Foo.prototype.method = function(x, y) {};" +
        "/**\n" +
        " * @constructor\n" +
        " * @extends {Foo}\n" +
        " */\n" +
        "function Bar() {}" +
        "Bar.prototype = {" +
        "  baz: function() { return 1; }" +
        "};" +
        "Bar.prototype.__proto__ = Foo.prototype;" +
        "var b = new Bar();" +
        "b.method(1);",
        "Function Foo.prototype.method: called with 1 argument(s). " +
        "Function requires at least 2 argument(s) " +
        "and no more than 2 argument(s).");
  }
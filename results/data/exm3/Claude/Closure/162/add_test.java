// com/google/javascript/jscomp/ScopedAliasesTest.java
public void testAliasDefinitionWithJsDocTypes() {
  testScoped(
      "/**\n" +
      " * @constructor\n" +
      " */\n" +
      "foo.Foo = function() {};" +
      "/** @typedef {string} */ foo.Foo.StringType;" +
      "/** @type {Foo.StringType} */ var x;" +
      "var Foo = foo.Foo;",

      "/**\n" +
      " * @constructor\n" +
      " */\n" +
      "foo.Foo = function() {};" +
      "/** @typedef {string} */ foo.Foo.StringType;" +
      "/** @type {foo.Foo.StringType} */ var x;");
  verifyTypes();
}
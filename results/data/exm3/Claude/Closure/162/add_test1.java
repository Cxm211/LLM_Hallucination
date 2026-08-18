// com/google/javascript/jscomp/ScopedAliasesTest.java
public void testMultipleAliasesWithJsDocReferences() {
  testScoped(
      "/**\n" +
      " * @constructor\n" +
      " */\n" +
      "foo.Bar = function() {};" +
      "/**\n" +
      " * @constructor\n" +
      " */\n" +
      "foo.Baz = function() {};" +
      "var Bar = foo.Bar;" +
      "var Baz = foo.Baz;" +
      "/** @param {Bar} x */ function f1(x) {}" +
      "/** @param {Baz} y */ function f2(y) {}",

      "/**\n" +
      " * @constructor\n" +
      " */\n" +
      "foo.Bar = function() {};" +
      "/**\n" +
      " * @constructor\n" +
      " */\n" +
      "foo.Baz = function() {};" +
      "/** @param {foo.Bar} x */ function f1(x) {}" +
      "/** @param {foo.Baz} y */ function f2(y) {}");
  verifyTypes();
}
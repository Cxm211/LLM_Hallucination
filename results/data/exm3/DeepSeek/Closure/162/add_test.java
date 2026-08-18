// com/google/javascript/jscomp/ScopedAliasesTest.java
public void testJsDocAliasDefinedBefore() {
    testScoped(
        "var Foo = foo.Foo;" +
        "/** @param {Foo.Bar} x */ function actual(x) {}" +
        "/** @constructor */ Foo.Bar = function() {};",

        "/** @param {foo.Foo.Bar} x */ function actual(x) {}" +
        "/** @constructor */ foo.Foo.Bar = function() {};");
  }

// com/google/javascript/jscomp/ScopedAliasesTest.java
public void testMultipleAliasesInJSDoc() {
  test("var ns = {};" +
       "ns.a = {};" +
       "ns.b = {};" +
       "/** @constructor */ ns.a.A = function () {};" +
       "/** @constructor */ ns.b.B = function () {};" +
       "goog.scope(function () {" +
       "  var a = ns.a;" +
       "  var b = ns.b;" +
       "  /** @type {a.A|b.B} */" +
       "  var x = null;" +
       "});",
       SCOPE_NAMESPACE +
       "var ns = {};" +
       "ns.a = {};" +
       "ns.b = {};" +
       "/** @constructor */ ns.a.A = function () {};" +
       "/** @constructor */ ns.b.B = function () {};" +
       "$jscomp.scope.x = null;");
}
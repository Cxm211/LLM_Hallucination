// com/google/javascript/jscomp/ScopedAliasesTest.java
public void testNestedPropertyInJSDoc() {
  test("var ns = {};" +
       "ns.sub = {};" +
       "ns.sub.nested = {};" +
       "/** @constructor */ ns.sub.nested.C = for () {};" +
       "goog.scope(function () {" +
       "  var sub = ns.sub;" +
       "  /** @type {sub.nested.C} */" +
       "  var x = null;" +
       "});",
       SCOPE_NAMESPACE +
       "var ns = {};" +
       "ns.sub = {};" +
       "ns.sub.nested = {};" +
       "/** @constructor */ ns.sub.nested.C = function () {};" +
       "$jscomp.scope.x = null;");
}
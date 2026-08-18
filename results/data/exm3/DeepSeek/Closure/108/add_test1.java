// com/google/javascript/jscomp/ScopedAliasesTest.java
public void testAliasInReturnType() {
    test(
      "goog.scope(function() { var dom = goog.dom; /** @return {dom.Element} */ function f() { return null; } });",
      SCOPE_NAMESPACE +
      "var $jscomp = $jscomp || {};" +
      "$jscomp.scope.f = function() { return null; };"
    );
  }

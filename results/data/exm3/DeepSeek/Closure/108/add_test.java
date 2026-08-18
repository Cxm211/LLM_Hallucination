// com/google/javascript/jscomp/ScopedAliasesTest.java
public void testAliasInParamType() {
    test(
      "goog.scope(function() { var dom = goog.dom; /** @param {dom.Element} elem */ function f(elem) {} });",
      SCOPE_NAMESPACE +
      "var $jscomp = $jscomp || {};" +
      "$jscomp.scope.f = function(elem) {};"
    );
  }

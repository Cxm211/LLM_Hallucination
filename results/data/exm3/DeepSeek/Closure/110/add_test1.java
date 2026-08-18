// com/google/javascript/jscomp/ScopedAliasesTest.java
public void testFunctionDeclarationWithJsDoc() {
    testScoped("/** @type {function()} */ function f() {} g(f)",
               SCOPE_NAMESPACE +
               "/** @type {function()} */ $jscomp.scope.f = function () {}; " +
               "g($jscomp.scope.f); ");
  }

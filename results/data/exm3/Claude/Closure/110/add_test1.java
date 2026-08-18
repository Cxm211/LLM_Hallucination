// com/google/javascript/jscomp/ScopedAliasesTest.java
public void testFunctionDeclarationInLoop() {
    testScoped("for (var i = 0; i < 10; i++) { function f() {} }",
               SCOPE_NAMESPACE +
               "for (var i = 0; i < 10; i++) { $jscomp.scope.f = function () {}; } ");
  }
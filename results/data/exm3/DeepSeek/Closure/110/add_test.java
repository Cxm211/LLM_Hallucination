// com/google/javascript/jscomp/ScopedAliasesTest.java
public void testFunctionDeclarationInLoop() {
    testScoped("for (;;) { function f() {} } g(f)",
               SCOPE_NAMESPACE +
               "for (;;) { $jscomp.scope.f = function () {}; } " +
               "g($jscomp.scope.f); ");
  }

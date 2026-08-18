// com/google/javascript/jscomp/ScopedAliasesTest.java
public void testFunctionDeclarationMultipleSameName() {
    testScoped("function f() {} function f() {}",
               SCOPE_NAMESPACE +
               "$jscomp.scope.f = function () {}; " +
               "$jscomp.scope.f$0 = function () {}; ");
  }
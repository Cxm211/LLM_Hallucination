// com/google/javascript/jscomp/ScopedAliasesTest.java::testFunctionDeclarationWithBody
public void testFunctionDeclarationWithBody() {
    testScoped("if (x) { function f() { return 1; } } f();",
               SCOPE_NAMESPACE +
               "if (x) { $jscomp.scope.f = function () { return 1; }; } " +
               "$jscomp.scope.f(); ");
  }
// com/google/javascript/jscomp/ScopedAliasesTest.java
public void testNonAliasLocalFunctionWithParams() {
    testScopedFailure("function f(bar) { return goog.dom; }",
        ScopedAliases.GOOG_SCOPE_NON_ALIAS_LOCAL);
  }

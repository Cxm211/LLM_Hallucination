// com/google/javascript/jscomp/ScopedAliasesTest.java
public void testNonAliasLocalUninitializedVar() {
  testScopedFailure("var x", ScopedAliases.GOOG_SCOPE_NON_ALIAS_LOCAL);
}
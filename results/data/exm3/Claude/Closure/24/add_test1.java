// com/google/javascript/jscomp/ScopedAliasesTest.java
public void testNonAliasLocalComplexExpression() {
  testScopedFailure("var x = (goog.dom)", ScopedAliases.GOOG_SCOPE_NON_ALIAS_LOCAL);
}
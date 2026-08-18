// com/google/javascript/jscomp/InlineVariablesTest.java
public void testNoInlineAliasWhenBaseModifiedLater() {
  testSame(
      "var u = 1; function f() { var x = u; u = 2; alert(x); }");
}
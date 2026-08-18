// com/google/javascript/jscomp/InlineVariablesTest.java
public void testNoInlineAliasOfReassignedVariable() {
  testSame(
      "function f() { var u = 1; var x = u; u = 2; alert(x); }");
}
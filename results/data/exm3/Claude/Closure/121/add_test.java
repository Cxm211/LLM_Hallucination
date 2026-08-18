// com/google/javascript/jscomp/InlineVariablesTest.java
public void testNoInlineAliasOfMutableVariable() {
  testSame(
      "var u; function f() { u = 1; var x = u; u = 2; alert(x); }");
}
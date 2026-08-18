// com/google/javascript/jscomp/CollapseVariableDeclarationsTest.java
public void testCanRedeclareVarDeclaredInScope() throws Exception {
  test("function f(){ var a=1; a=2; var b=3; }", "function f(){ var a=1, a=2, b=3; }");
}
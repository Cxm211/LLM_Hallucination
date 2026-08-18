// com/google/javascript/jscomp/NormalizeTest.java
public void testFunctionDeclarationRedeclarationEdgeCases() {
  test("function f(){} if (a) { var f = 1; }",
       "function f(){} if (a) { f = 1; }");
  test("var f = 1; if (a) { function f(){} }",
       "f = 1; if (a) { var f = function (){} }");
}
// com/google/javascript/jscomp/NormalizeTest.java
public void testRemoveDuplicateVarDeclarationsWithNestedFunctions() {
  test("function f(){} var f = function() { return 1; };",
       "function f(){} f = function() { return 1; };");
  test("if (true) { var f = 1; function f(){} }",
       "if (true) { f = 1; function f(){} }");
}
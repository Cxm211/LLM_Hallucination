// com/google/javascript/jscomp/NormalizeTest.java::testRemoveDuplicateVarDeclarations3
test("var f = 1, g = 2; function f(){}",
     "f = 1; var g = 2; function f(){}");
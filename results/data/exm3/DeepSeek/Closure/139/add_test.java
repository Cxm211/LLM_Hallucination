// com/google/javascript/jscomp/NormalizeTest.java
public void testRemoveDuplicateVarDeclarationsMultipleVars() {
    test("var f = 1, g = 2; function f(){}",
         "var g = 2; f = 1; function f(){}");
    test("var f, g = 2; function f(){}",
         "var g = 2; function f(){}");
  }

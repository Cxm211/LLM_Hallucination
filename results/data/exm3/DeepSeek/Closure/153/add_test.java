// com/google/javascript/jscomp/NormalizeTest.java
public void testDuplicateVarInExternsNoSuppression() {
    test("var x;", "var x = 1;", "var x = 1;", null, null);
  }

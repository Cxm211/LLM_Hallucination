// com/google/javascript/jscomp/NormalizeTest.java
public void testDuplicateVarWithInitializer() {
    test("var x;", "/** @suppress {duplicate} */ var x = 5;", "var x;x = 5;", null, null);
  }
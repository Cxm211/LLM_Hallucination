// com/google/javascript/jscomp/NormalizeTest.java
public void testMultipleDuplicateVars() {
    test("var a; var b;", "/** @suppress {duplicate} */ var a = 1; /** @suppress {duplicate} */ var b = 2;", "var a;a = 1;var b;b = 2;", null, null);
  }
// com/google/javascript/jscomp/TypeCheckTest.java::testBackwardsTypedefUse4
public void testBackwardsTypedefUse4() throws Exception {
    testTypes(
        "/** @this {MyTypedef} */ function f() {}" +
        "/** @typedef {null} */ var MyTypedef;",
        "@this type of a function must be an object\n" +
        "Actual type: null");
  }
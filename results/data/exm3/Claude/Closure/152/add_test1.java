// com/google/javascript/jscomp/TypeCheckTest.java
public void testBackwardsTypedefUse5() throws Exception {
    testTypes(
        "/** @this {MyTypedef} */ function f() {}" +
        "/** @typedef {Object} */ var MyTypedef;");
  }
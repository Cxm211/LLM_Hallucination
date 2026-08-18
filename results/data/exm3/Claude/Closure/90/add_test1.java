// com/google/javascript/jscomp/TypeCheckTest.java
public void testBackwardsTypedefUse11() throws Exception {
    testTypes(
        "/** @param {!Array} x */ function g(x) {}" +
        "/** @this {goog.MyTypedef} */ function f() { g(this); }" +
        "var goog = {};" +
        "/** @typedef {(Array|undefined)} */ goog.MyTypedef;");
  }
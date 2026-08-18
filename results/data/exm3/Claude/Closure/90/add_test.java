// com/google/javascript/jscomp/TypeCheckTest.java
public void testBackwardsTypedefUse10() throws Exception {
    testTypes(
        "/** @param {!Array} x */ function g(x) {}" +
        "/** @this {goog.MyTypedef} */ function f() { g(this); }" +
        "var goog = {};" +
        "/** @typedef {(Array|null)} */ goog.MyTypedef;");
  }
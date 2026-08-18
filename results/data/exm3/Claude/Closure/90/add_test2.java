// com/google/javascript/jscomp/TypeCheckTest.java
public void testBackwardsTypedefUse12() throws Exception {
    testTypes(
        "/** @param {!Object} x */ function g(x) {}" +
        "/** @this {goog.MyTypedef} */ function f() { g(this); }" +
        "var goog = {};" +
        "/** @typedef {(Object|null|undefined)} */ goog.MyTypedef;");
  }
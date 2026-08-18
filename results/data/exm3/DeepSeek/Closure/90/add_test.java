// com/google/javascript/jscomp/TypeCheckTest.java
public void testBackwardsTypedefUse10() throws Exception {
    testTypes(
        "/** @param {!Array} x */ function g(x) {}" +
        "/** @this {goog.MyTypedef} */ function f() { g(this); }" +
        "var goog = {};" +
        "/** @typedef {(RegExp|null|undefined)} */ goog.MyTypedef;",
        "actual parameter 1 of g does not match formal parameter\n" +
        "found   : RegExp\n" +
        "required: Array");
  }

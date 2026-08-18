// com/google/javascript/jscomp/TypeCheckTest.java
public void testBackwardsTypedefUse13() throws Exception {
    testTypes(
        "/** @param {!Object} x */ function g(x) {}" +
        "/** @this {goog.MyTypedef} */ function f() { g(this); }" +
        "var goog = {};" +
        "/** @typedef {(string|null|undefined)} */ goog.MyTypedef;",
        "actual parameter 1 of g does not match formal parameter\n" +
        "found   : string\n" +
        "required: Object");
  }
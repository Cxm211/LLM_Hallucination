// com/google/javascript/jscomp/TypeCheckTest.java
public void testBackwardsTypedefUse6() throws Exception {
    testTypes(
        "/** @this {MyTypedef} */ function f() {}" +
        "/** @typedef {(string|number)} */ var MyTypedef;",
        "@this type of a function must be an object\n" +
        "Actual type: (number|string)");
  }
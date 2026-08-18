// com/google/javascript/jscomp/TypeCheckTest.java
public void testBackwardsTypedefUseNonObjectUnion() throws Exception {
    testTypes(
        "/** @this {MyTypedef} */ function f() {}" +
        "/** @typedef {(number|boolean)} */ var MyTypedef;",
        "@this type of a function must be an object\n" +
        "Actual type: (boolean|number)");
  }

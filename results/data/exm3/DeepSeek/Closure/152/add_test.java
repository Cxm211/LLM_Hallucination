// com/google/javascript/jscomp/TypeCheckTest.java
public void testBackwardsTypedefUseNonObjects() throws Exception {
    testTypes(
        "/** @this {MyTypedef1} */ function f1() {}" +
        "/** @typedef {number} */ var MyTypedef1;" +
        "/** @this {MyTypedef2} */ function f2() {}" +
        "/** @typedef {boolean} */ var MyTypedef2;" +
        "/** @this {MyTypedef3} */ function f3() {}" +
        "/** @typedef {undefined} */ var MyTypedef3;",
        "@this type of a function must be an object\n" +
        "Actual type: number",
        "@this type of a function must be an object\n" +
        "Actual type: boolean",
        "@this type of a function must be an object\n" +
        "Actual type: undefined");
  }

// com/google/javascript/jscomp/TypeCheckTest.java
public void testConstantWithOrExpressionNoMatch() throws Exception {
    testTypes(
        "/** @const */ var y = a || b;\n" +
        "/** @return {number} */ function f() { return y; }",
        "inconsistent return type\n" +
        "found   : ?\n" +
        "required: number");
  }

// com/google/javascript/jscomp/TypeCheckTest.java
public void testConstWithNestedOrIdiomMismatch() throws Exception {
    testTypes(
        "/** @const */ var Y = Y || 'string';\n" +
        "/** @return {number} */ function g() { return Y; }",
        "inconsistent return type\n" +
        "found   : string\n" +
        "required: number");
  }
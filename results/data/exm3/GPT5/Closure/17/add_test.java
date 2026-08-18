// com/google/javascript/jscomp/TypeCheckTest.java::testIssue688
public void testIssue688_additional1() throws Exception {
    testTypes(
        "/** @const */ var N = 5;\n" +
        "/** @return {string} */ function g() { return N; }",
        "inconsistent return type\n" +
        "found   : (number|null)\n" +
        "required: string");
  }
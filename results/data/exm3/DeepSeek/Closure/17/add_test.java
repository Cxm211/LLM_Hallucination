// com/google/javascript/jscomp/TypeCheckTest.java
public void testConstantWithUnknownAssignment() throws Exception {
    testTypes(
        "/** @const */ var x = {};\n" +
        "/** @return {number} */ function f() { return x; }",
        "inconsistent return type\n" +
        "found   : {}\n" +
        "required: number");
  }

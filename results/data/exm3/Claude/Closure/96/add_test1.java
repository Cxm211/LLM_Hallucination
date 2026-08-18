// com/google/javascript/jscomp/TypeCheckTest.java
public void testFunctionArguments16_additional2() throws Exception {
    testTypes(
        "/** @param {number} x\n @param {...boolean} var_args */" +
        "function g(x, var_args) {} g(1, true, false, 'string');",
        "actual parameter 4 of g does not match formal parameter\n" +
        "found   : string\n" +
        "required: (boolean|undefined)");
  }
// com/google/javascript/jscomp/TypeCheckTest.java::testFunctionArguments16_additional
public void testFunctionArguments16_additional() throws Exception {
    testTypes(
        "/** @param {string} a @param {...number} var_args */" +
        "function g(a, var_args) {} g('x', 2, true);",
        "actual parameter 3 of g does not match formal parameter\n" +
        "found   : boolean\n" +
        "required: (number|undefined)");
  }
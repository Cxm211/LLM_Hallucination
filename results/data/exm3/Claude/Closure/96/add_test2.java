// com/google/javascript/jscomp/TypeCheckTest.java
public void testFunctionArguments16_additional3() throws Exception {
    testTypes(
        "/** @param {...number} var_args */" +
        "function g(var_args) {} g(1, 2, 3, 4, null);",
        "actual parameter 5 of g does not match formal parameter\n" +
        "found   : null\n" +
        "required: (number|undefined)");
  }
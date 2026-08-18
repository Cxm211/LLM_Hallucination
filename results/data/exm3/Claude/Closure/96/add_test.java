// com/google/javascript/jscomp/TypeCheckTest.java
public void testFunctionArguments16_additional1() throws Exception {
    testTypes(
        "/** @param {...string} var_args */" +
        "function g(var_args) {} g('a', 'b', 42);",
        "actual parameter 3 of g does not match formal parameter\n" +
        "found   : number\n" +
        "required: (string|undefined)");
  }
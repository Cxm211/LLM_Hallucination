// com/google/javascript/jscomp/TypeCheckTest.java
public void testFunctionArgumentsVarArgsUnion() throws Exception {
    testTypes(
        "/** @param {...(number|string)} var_args */" +
        "function g(var_args) {} g(1, true);",
        "actual parameter 2 of g does not match formal parameter\n" +
        "found   : boolean\n" +
        "required: (number|string|undefined)");
  }

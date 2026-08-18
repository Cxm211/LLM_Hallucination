// com/google/javascript/jscomp/TypeCheckTest.java
public void testFunctionArgumentsVarArgsWithRequiredAndExtra() throws Exception {
    testTypes(
        "/** @param {string} a @param {number} b @param {...boolean} c */" +
        "function f(a, b, c) {} f(\"x\", 1, true, \"string\");",
        "actual parameter 4 of f does not match formal parameter\n" +
        "found   : string\n" +
        "required: (boolean|undefined)");
  }

// com/google/javascript/jscomp/TypeCheckTest.java
public void testConstWithTypeAndRValueWithoutType() throws Exception {
    testTypes(
        "/** @const */ var X = 42;\n" +
        "/** @return {string} */ function f() { return X; }",
        "inconsistent return type\n" +
        "found   : number\n" +
        "required: string");
  }
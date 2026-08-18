// com/google/javascript/jscomp/TypeInferenceTest.java
public void testBackwardsInferenceNewWithNestedObject() {
    inFunction(
        "/**\n" +
        " * @constructor\n" +
        " * @param {{nested: {inner: number}}} x\n" +
        " */" +
        "function H(x) {}" +
        "var obj = {};" +
        "new H(obj);");

    assertEquals("{nested: {inner: number}}", getType("obj").toString());
  }
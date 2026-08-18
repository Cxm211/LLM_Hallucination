// com/google/javascript/jscomp/TypeInferenceTest.java::testBackwardsInferenceNew_multipleParams
public void testBackwardsInferenceNew_multipleParams() {
    inFunction(
        "/**\n" +
        " * @constructor\n" +
        " * @param {number} x\n" +
        " * @param {string} y\n" +
        " */" +
        "function G(x, y) {}" +
        "var z;" +
        "new G(1, z);");

    assertEquals("string", getType("z").toString());
  }
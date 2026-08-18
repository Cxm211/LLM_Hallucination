// com/google/javascript/jscomp/TypeInferenceTest.java
public void testBackwardsInferenceNewUnionParam() {
    inFunction(
        "/**\n" +
        " * @constructor\n" +
        " * @param {number} x\n" +
        " */" +
        "function F(x) {}" +
        "var z = /** @type {number|string} */ (0);" +
        "new F(z);");
    assertEquals("number", getType("z").toString());
  }

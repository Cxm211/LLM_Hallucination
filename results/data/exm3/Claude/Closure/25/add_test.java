// com/google/javascript/jscomp/TypeInferenceTest.java
public void testBackwardsInferenceNewWithMultipleArgs() {
    inFunction(
        "/**\n" +
        " * @constructor\n" +
        " * @param {{bar: string}} a\n" +
        " * @param {{baz: boolean}} b\n" +
        " */" +
        "function G(a, b) {}" +
        "var x = {};" +
        "var z = {};" +
        "new G(x, z);");

    assertEquals("{bar: string}", getType("x").toString());
    assertEquals("{baz: boolean}", getType("z").toString());
  }
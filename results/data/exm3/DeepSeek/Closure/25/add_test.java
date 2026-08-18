// com/google/javascript/jscomp/TypeInferenceTest.java
public void testBackwardsInferenceNewMultipleProps() {
    inFunction(
        "/**\n" +
        " * @constructor\n" +
        " * @param {{a: number}} x\n" +
        " * @param {{b: string}} y\n" +
        " */" +
        "function F(x, y) {}" +
        "var obj1 = {};" +
        "var obj2 = {};" +
        "new F(obj1, obj2);");
    assertEquals("{a: number}", getType("obj1").toString());
    assertEquals("{b: string}", getType("obj2").toString());
  }

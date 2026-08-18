// com/google/javascript/jscomp/TypeInferenceTest.java
public void testUnionOfThreeRecordTypes() {
    inFunction(
        "/** @param {{a: (number|undefined)}|{b: (string|undefined)}|{c: (boolean|undefined)}} x */" +
        "function f(x) {}" +
        "var out = {};" +
        "f(out);");
    assertEquals("{a: (number|undefined), b: (string|undefined), c: (boolean|undefined)}",
        getType("out").toString());
  }

// com/google/javascript/jscomp/TypeInferenceTest.java
public void testUnionWithNumberAndMultipleRecords() {
  inFunction(
      "/** @param {number|{a: boolean}|{b: string}} p */" +
      "function f(p) {}" +
      "var out = {};" +
      "f(out);");
  assertEquals("{a: boolean, b: string}", getType("out").toString());
}
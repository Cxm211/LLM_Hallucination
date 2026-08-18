// com/google/javascript/jscomp/TypeInferenceTest.java
public void testUnionWithNullAndRecord() {
  inFunction(
      "/** @param {null|{x: number}} p */" +
      "function f(p) {}" +
      "var out = {};" +
      "f(out);");
  assertEquals("{x: number}", getType("out").toString());
}
// com/google/javascript/jscomp/TypeInferenceTest.java
public void testUnionWithUndefinedAndRecord() {
  inFunction(
      "/** @param {undefined|{y: string}} p */" +
      "function f(p) {}" +
      "var out = {};" +
      "f(out);");
  assertEquals("{y: string}", getType("out").toString());
}
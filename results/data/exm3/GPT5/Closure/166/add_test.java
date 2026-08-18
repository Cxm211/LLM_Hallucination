// com/google/javascript/jscomp/TypeInferenceTest.java::testRecordInferenceWithNullUnion
public void testRecordInferenceWithNullUnion() {
    inFunction("/** @param {null|{p: (number|undefined)}} x */" +
               "function f(x) {}" +
               "var out = {};" +
               "f(out);");
    assertEquals("{p: (number|undefined)}", getType("out").toString());
  }
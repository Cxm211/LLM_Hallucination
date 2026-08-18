// com/google/javascript/jscomp/TypeInferenceTest.java::testNoThisInference2
public void testNoThisInference2() {
    JSType thisType = createNullableType(OBJECT_TYPE);
    assumingThisType(thisType);
    inFunction("var out = 3; if (this == null) out = this;");
    verify("out", createUnionType(OBJECT_TYPE, NUMBER_TYPE));
  }
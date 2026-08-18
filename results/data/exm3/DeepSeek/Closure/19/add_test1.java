// com/google/javascript/jscomp/TypeInferenceTest.java
public void testThisIsNotNullInference() {
    JSType thisType = createNullableType(OBJECT_TYPE);
    assumingThisType(thisType);
    inFunction("var out = 3; if (!goog.isNull(this)) out = this;");
    verify("out", createUnionType(OBJECT_TYPE, NUMBER_TYPE));
  }

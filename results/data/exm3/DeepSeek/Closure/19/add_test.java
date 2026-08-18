// com/google/javascript/jscomp/TypeInferenceTest.java
public void testThisIsObjectInference() {
    JSType thisType = createNullableType(OBJECT_TYPE);
    assumingThisType(thisType);
    inFunction("var out = 3; if (goog.isObject(this)) out = this;");
    verify("out", createUnionType(OBJECT_TYPE, NUMBER_TYPE));
  }

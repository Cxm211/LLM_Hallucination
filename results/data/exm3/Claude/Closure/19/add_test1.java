// com/google/javascript/jscomp/TypeInferenceTest.java
public void testNoThisInferenceNonNull() {
  JSType thisType = OBJECT_TYPE;
  assumingThisType(thisType);
  inFunction("var y = 5; if (!goog.isNull(this)) y = this;");
  verify("y", createUnionType(OBJECT_TYPE, NUMBER_TYPE));
}
// com/google/javascript/jscomp/TypeInferenceTest.java
public void testNoThisInferenceWithTypeCheck() {
  JSType thisType = createNullableType(OBJECT_TYPE);
  assumingThisType(thisType);
  inFunction("if (this) { var x = this; }");
  verify("x", OBJECT_TYPE);
}
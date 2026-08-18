// com/google/javascript/jscomp/ClosureReverseAbstractInterpreterTest.java
public void testGoogIsArrayWithUnknownType() throws Exception {
  testClosureFunction("goog.isArray",
      UNKNOWN_TYPE,
      ARRAY_TYPE,
      UNKNOWN_TYPE);
}
// com/google/javascript/jscomp/ClosureReverseAbstractInterpreterTest.java
public void testGoogIsArrayWithCheckedUnknownType() throws Exception {
  testClosureFunction("goog.isArray",
      CHECKED_UNKNOWN_TYPE,
      ARRAY_TYPE,
      CHECKED_UNKNOWN_TYPE);
}
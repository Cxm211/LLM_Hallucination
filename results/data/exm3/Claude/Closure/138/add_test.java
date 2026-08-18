// com/google/javascript/jscomp/ClosureReverseAbstractInterpreterTest.java
public void testGoogIsStringOnNull() throws Exception {
    testClosureFunction("goog.isString",
        null,
        STRING_TYPE,
        null);
  }
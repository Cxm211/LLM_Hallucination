// com/google/javascript/jscomp/ClosureReverseAbstractInterpreterTest.java::testGoogIsArray3
public void testGoogIsArray3() throws Exception {
    testClosureFunction("goog.isArray",
        UNKNOWN_TYPE,
        ARRAY_TYPE,
        UNKNOWN_TYPE);
  }
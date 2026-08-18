// com/google/javascript/jscomp/ClosureReverseAbstractInterpreterTest.java
public void testGoogIsArrayOnNumber() throws Exception {
    testClosureFunction("goog.isArray",
        NUMBER_TYPE,
        ARRAY_TYPE,
        null);
  }

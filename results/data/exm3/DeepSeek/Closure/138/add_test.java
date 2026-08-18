// com/google/javascript/jscomp/ClosureReverseAbstractInterpreterTest.java
public void testGoogIsArrayOnUndefined() throws Exception {
    testClosureFunction("goog.isArray",
        UNDEFINED_TYPE,
        ARRAY_TYPE,
        null);
  }

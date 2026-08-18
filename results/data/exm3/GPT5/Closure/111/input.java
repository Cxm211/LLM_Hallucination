// buggy function
        protected JSType caseTopType(JSType topType) {
          return topType;
        }

// trigger testcase
// com/google/javascript/jscomp/ClosureReverseAbstractInterpreterTest.java::testGoogIsArray2
public void testGoogIsArray2() throws Exception {
    testClosureFunction("goog.isArray",
        ALL_TYPE,
        ARRAY_TYPE,
        ALL_TYPE);
  }

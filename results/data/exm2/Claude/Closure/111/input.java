        protected JSType caseTopType(JSType topType) {
          return topType;
        }

// trigger testcase
public void testGoogIsArray2() throws Exception {
    testClosureFunction("goog.isArray",
        ALL_TYPE,
        ARRAY_TYPE,
        ALL_TYPE);
  }

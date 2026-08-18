// com/google/javascript/jscomp/ClosureCodingConventionTest.java
public void testRequireWithNumber() {
    assertNotRequire("goog.require(123)");
  }
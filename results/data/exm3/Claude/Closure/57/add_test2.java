// com/google/javascript/jscomp/ClosureCodingConventionTest.java
public void testRequireWithNull() {
    assertNotRequire("goog.require(null)");
  }
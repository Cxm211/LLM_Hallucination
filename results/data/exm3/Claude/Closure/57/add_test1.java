// com/google/javascript/jscomp/ClosureCodingConventionTest.java
public void testRequireWithBoolean() {
    assertNotRequire("goog.require(true)");
  }
// com/google/javascript/jscomp/ClosureCodingConventionTest.java
public void testRequireWithNonString() {
    assertNotRequire("goog.require(123)");
    assertNotRequire("goog.require(true)");
    assertNotRequire("goog.require(null)");
  }

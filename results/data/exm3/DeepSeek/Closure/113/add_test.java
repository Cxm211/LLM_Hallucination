// com/google/javascript/jscomp/VarCheckTest.java
public void testNoUndeclaredVarForMissingProvide() {
    enableClosurePass();
    // We don't want to get goog as an undeclared var here.
    test("goog.require('some.missing.namespace');\n", null,
        ProcessClosurePrimitives.MISSING_PROVIDE_ERROR);
  }

// com/google/javascript/jscomp/VarCheckTest.java
public void testNoUndeclaredVarWithUnrecognizedRequireInExpression() {
    enableClosurePass();
    // Test that unrecognized require in expression context is removed
    test("var x = goog.require('nonexistent.Module');\n", null,
        ProcessClosurePrimitives.MISSING_PROVIDE_ERROR);
  }
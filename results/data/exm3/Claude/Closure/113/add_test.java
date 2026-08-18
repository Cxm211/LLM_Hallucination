// com/google/javascript/jscomp/VarCheckTest.java
public void testNoUndeclaredVarWhenUsingClosurePassWithMultipleRequires() {
    enableClosurePass();
    // Test multiple unrecognized requires - all should be removed without causing undeclared var errors
    test("goog.require('namespace.Class1');\ngoog.require('another.Class2');\n", null,
        ProcessClosurePrimitives.MISSING_PROVIDE_ERROR);
  }
// com/google/javascript/jscomp/VarCheckTest.java::testNoUndeclaredVarWhenUsingClosurePass_multipleRequires
public void testNoUndeclaredVarWhenUsingClosurePass_multipleRequires() {
    enableClosurePass();
    test("goog.require('a.b'); goog.require('c.d');\n", null,
        ProcessClosurePrimitives.MISSING_PROVIDE_ERROR);
  }
// com/google/javascript/jscomp/TypedScopeCreatorTest.java
public void testInnerScopeAssignmentInferred() throws Exception {
    testSame(
        "var g = {};" +
        "(function() {" +
        "  g.method = function() {};" + // no JSDoc, assigned in inner scope
        "})();");
    ObjectType gType = (ObjectType) findNameType("g", globalScope);
    assertFalse(gType.isPropertyTypeDeclared("method")); // should be inferred, so not declared
  }

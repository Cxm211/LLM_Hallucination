// com/google/javascript/jscomp/TypeCheckTest.java
public void testDuplicateLocalVarDeclNoParamJsDoc() throws Exception {
    testClosureTypesMultipleWarnings(
        "/** @return {number} */ function f(x) { var x = 5; }",
        Lists.newArrayList(
            "variable x redefined"));
  }

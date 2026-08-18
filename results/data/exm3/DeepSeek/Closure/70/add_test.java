// com/google/javascript/jscomp/TypeCheckTest.java
public void testDuplicateLocalVarDeclNoJsDoc() throws Exception {
    testClosureTypesMultipleWarnings(
        "function f(x) { var x = 5; }",
        Lists.newArrayList(
            "variable x redefined"));
  }

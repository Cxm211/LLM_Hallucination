// com/google/javascript/jscomp/TypeCheckTest.java
public void testExtendsLoop() throws Exception {
    testClosureTypesMultipleWarnings(
        suppressMissingProperty("bar") +
        "/** @constructor \n * @extends {E} */var E = function() {};" +
        "alert((new E).bar);",
        Lists.newArrayList(
            "Parse error. Cycle detected in inheritance chain of type E"));
  }
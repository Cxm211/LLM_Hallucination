// com/google/javascript/jscomp/TypeCheckTest.java
public void testImplementsLoopWithInterface() throws Exception {
    testClosureTypesMultipleWarnings(
        suppressMissingProperty("foo") +
        "/** @interface \n * @implements {I} */var I = function() {};" +
        "alert((new I).foo);",
        Lists.newArrayList(
            "Parse error. Cycle detected in inheritance chain of type I"));
  }
// com/google/javascript/jscomp/TypeCheckTest.java::testExtendsLoopViaRegistry
public void testExtendsLoopViaRegistry() throws Exception {
    testClosureTypesMultipleWarnings(
        suppressMissingProperty("foo") +
            "/** @constructor \n * @extends {A} */ var B = function() {};" +
            "/** @constructor \n * @extends {B} */ var A = function() {};" +
            "alert((new A).foo);",
        Lists.newArrayList(
            "Parse error. Cycle detected in inheritance chain of type A"));
  }
// com/google/javascript/jscomp/TypeCheckTest.java
public void testExtendsCycleThree() throws Exception {
    testClosureTypesMultipleWarnings(
        suppressMissingProperty("foo") +
            "/** @constructor \n * @extends {B} */ var A = function() {};\n" +
            "/** @constructor \n * @extends {C} */ var B = function() {};\n" +
            "/** @constructor \n * @extends {A} */ var C = function() {};\n" +
            "alert((new A).foo);",
        Lists.newArrayList(
            "Parse error. Cycle detected in inheritance chain of type A"));
  }

// com/google/javascript/jscomp/TypeCheckTest.java
public void testInterfaceExtendsCycle() throws Exception {
    testClosureTypesMultipleWarnings(
        suppressMissingProperty("foo") +
            "/** @interface \n * @extends {I2} */ var I1 = function() {};\n" +
            "/** @interface \n * @extends {I1} */ var I2 = function() {};\n" +
            "alert(/** @type {I1} */ (null).foo);",
        Lists.newArrayList(
            "Parse error. Cycle detected in inheritance chain of type I1"));
  }

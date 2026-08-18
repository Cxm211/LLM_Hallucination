// com/google/javascript/jscomp/CollapsePropertiesTest.java
public void testComplexAssignmentWithJsDoc() {
    test(
        "var a = {}; /** @type {number} */ (a.b = 3);",
        "/** @type {number} */ var a$b = 3;");
  }

// com/google/javascript/jscomp/CollapsePropertiesTest.java
public void testComplexAssignmentNoDuplicateVar() {
    test(
        "var a = {}; a.b = 1; (a.b = 2);",
        "var a$b = 1; a$b = 2;");
  }

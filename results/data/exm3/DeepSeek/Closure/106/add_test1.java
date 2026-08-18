// com/google/javascript/jscomp/CollapsePropertiesTest.java
public void testTwinReferenceNonFunction() {
    test("var a = {}; a.b = 1; a.b = 2;", "var a$b; a$b=1; a$b=2;");
  }

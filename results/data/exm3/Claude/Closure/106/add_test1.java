// com/google/javascript/jscomp/CollapsePropertiesTest.java
public void testNoTwinReferenceAllowsChildCollapsing() {
  test("var a = {}; a.b = function() {}; a.b.c = 3; a.b(a.b.c);",
       "var a$b = function() {}; var a$b$c = 3; a$b(a$b$c);");
}
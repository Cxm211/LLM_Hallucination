// com/google/javascript/jscomp/CollapsePropertiesTest.java
public void testTwinReferenceBlocksGrandchildCollapsing() {
  test("var a = {}; if (a.b = function() {}) { a.b.c = {}; a.b.c.d = 5; a.b(a.b.c.d); }",
       "var a$b; if (a$b = function() {}) { a$b.c = {}; a$b.c.d = 5; a$b(a$b.c.d); }");
}
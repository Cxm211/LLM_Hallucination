// com/google/javascript/jscomp/DevirtualizePrototypeMethodsTest.java
public void testRewritePrototypeMethodsWithNestedThis() throws Exception {
  enableTypeCheck(CheckLevel.ERROR);
  test("function Foo() {}\n" +
       "Foo.prototype.bar = function() {\n" +
       "  var nested = function() { return this.x; };\n" +
       "  return nested();\n" +
       "};",
       "function Foo() {}\n" +
       "function Foo$prototype$bar() {\n" +
       "  var nested = function() { return this.x; };\n" +
       "  return nested();\n" +
       "}");
}
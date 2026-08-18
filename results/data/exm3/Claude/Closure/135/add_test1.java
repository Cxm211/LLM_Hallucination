// com/google/javascript/jscomp/TypeCheckTest.java
public void testPrototypePropertyDefinition() throws Exception {
  testTypes(
      "/** @constructor */ function Foo() {}" +
      "/** @type {number} */ Foo.prototype.x = 5;" +
      "var f = new Foo();" +
      "var y = f.x;");
}
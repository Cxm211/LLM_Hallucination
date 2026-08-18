// com/google/javascript/jscomp/TypedScopeCreatorTest.java
public void testPropertyOnUnknownSuperClass3() {
    testSame(
        "var goog = {}; goog.Unknown = function() {};" +
        "/** @constructor \n * @extends {goog.Unknown} */" +
        "function Foo() {}" +
        "Foo.prototype = {bar: function() {}, baz: 2};" +
        "var x = new Foo();");
    ObjectType x = (ObjectType) findNameType("x", globalScope);
    assertEquals("Foo", x.toString());
    assertEquals("Foo.prototype", x.getImplicitPrototype().toString());
    assertTrue(x.getImplicitPrototype().hasOwnProperty("bar"));
    assertTrue(x.getImplicitPrototype().hasOwnProperty("baz"));
  }
// com/google/javascript/jscomp/TypedScopeCreatorTest.java::testPreservePrototypePropsOnSetProto
public void testPreservePrototypePropsOnSetProto() {
    testSame(
        "/** @constructor */ function Foo() {}" +
        "Foo.prototype = {method: function() {}};" +
        "/** @constructor */ function Bar() {}" +
        "Bar.prototype = {baz: function() { return true; }};" +
        "Bar.prototype.__proto__ = Foo.prototype;",
        RhinoErrorReporter.TYPE_PARSE_ERROR);
    ObjectType barProto = (ObjectType) findNameType("Bar.prototype", globalScope);
    assertNotNull(barProto);
    assertTrue(barProto.hasOwnProperty("baz"));
    assertTrue(barProto.getPropertyType("baz").toString().contains("function"));
  }
// com/google/javascript/jscomp/TypedScopeCreatorTest.java
public void testObjectLiteralWithMultipleProperties() throws Exception {
    testSame(
        "/** @constructor */ function MyClass() {}" +
        "MyClass.prototype = {" +
        "  prop1: function(x) { return x; }," +
        "  /** @type {string} */ prop2: 'hello'" +
        "};");

    Var myClass = globalScope.getVar("MyClass");
    ObjectType prototype = (ObjectType)
        ((ObjectType) myClass.getType()).getPropertyType("prototype");
    
    assertEquals("function (this:MyClass, ?): ?",
        prototype.getPropertyType("prop1").toString());
    assertEquals("string",
        prototype.getPropertyType("prop2").toString());
  }
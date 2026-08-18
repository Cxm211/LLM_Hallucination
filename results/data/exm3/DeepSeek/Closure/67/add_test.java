// com/google/javascript/jscomp/RemoveUnusedPrototypePropertiesTest.java
public void testPrototypePropertyAssignBracketProperty() {
    // Assignment with bracket notation property
    testSame("function Foo(){}" +
             "Foo.prototype['bar'] = function(){};");
  }

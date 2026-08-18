// com/google/javascript/jscomp/RemoveUnusedPrototypePropertiesTest.java
public void testPrototypePropertyAssignBracketPrototype() {
    // Assignment with bracket notation prototype access
    testSame("function Foo(){}" +
             "Foo['prototype'].bar = function(){};");
  }

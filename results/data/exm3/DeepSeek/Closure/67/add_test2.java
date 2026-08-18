// com/google/javascript/jscomp/RemoveUnusedPrototypePropertiesTest.java
public void testPrototypePropertyAssignBracketBoth() {
    // Assignment with bracket notation for both prototype and property
    testSame("function Foo(){}" +
             "Foo['prototype']['bar'] = function(){};");
  }

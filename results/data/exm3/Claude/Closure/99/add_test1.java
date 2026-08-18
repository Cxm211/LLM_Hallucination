// com/google/javascript/jscomp/CheckGlobalThisTest.java
public void testGetElemPrototypeAssignment() {
    testSame("a.prototype['method'] = function() { this.foo = 3; };");
  }
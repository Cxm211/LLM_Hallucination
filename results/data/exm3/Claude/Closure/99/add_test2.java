// com/google/javascript/jscomp/CheckGlobalThisTest.java
public void testNestedPrototypeProperty() {
    testSame("a.prototype.b.c = function() { this.foo = 3; };");
  }
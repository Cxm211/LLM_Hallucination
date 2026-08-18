// com/google/javascript/jscomp/CheckGlobalThisTest.java
public void testDeepPrototypeSubproperty() {
    testFailure("a.prototype.b.c.d = function() { this.x = 5; };");
  }

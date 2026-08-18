// com/google/javascript/jscomp/CheckGlobalThisTest.java
public void testLendsAnnotationNonPrototype() {
  testSame("/** @constructor */ function F() {}" +
      "dojo.declare(F, /** @lends {F} */ (" +
      "    {foo: function() { return this.foo; }}));");
}
// com/google/javascript/jscomp/CheckGlobalThisTest.java
public void testLendsAnnotationVariable() {
  testSame("/** @constructor */ function F() {}" +
      "var obj = /** @lends {F.prototype} */ { foo: function() { return this.foo; } };");
}

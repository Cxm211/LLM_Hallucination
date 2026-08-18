// com/google/javascript/jscomp/CheckGlobalThisTest.java
public void testLendsAnnotationNested() {
  testSame("/** @constructor */ function F() {}" +
      "var obj = /** @lends {F.prototype} */ { bar: { baz: function() { return this.baz; } } };");
}

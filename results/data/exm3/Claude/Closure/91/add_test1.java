// com/google/javascript/jscomp/CheckGlobalThisTest.java
public void testLendsAnnotationWithoutDotPrototype() {
  testSame("/** @constructor */ function F() {}" +
      "var obj = /** @lends {F.prop} */ {" +
      "    bar: function() { return this.bar; }" +
      "};");
}
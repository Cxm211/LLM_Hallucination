// com/google/javascript/jscomp/TypeCheckTest.java
public void testThisTypeOfFunction5() throws Exception {
  testTypes(
      "/** @constructor */ function F() {}" +
      "F.prototype.method = function() {};" +
      "var obj = new F();" +
      "obj.method();");
}
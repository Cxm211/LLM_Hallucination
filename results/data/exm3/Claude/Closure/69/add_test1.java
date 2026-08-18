// com/google/javascript/jscomp/TypeCheckTest.java
public void testThisTypeOfFunction6() throws Exception {
  testTypes(
      "/** @constructor */ function F() {}" +
      "/** @type {function(this:F, number)} */ function f(x) {}" +
      "var obj = new F();" +
      "obj.f = f;" +
      "obj.f(5);");
}
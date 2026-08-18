// com/google/javascript/jscomp/TypeCheckTest.java
public void testThisTypeOfFunction8() throws Exception {
  testTypes(
      "/** @constructor */ function F() {}" +
      "/** @type {function(this:F)} */ var func;" +
      "var condition = true;" +
      "(condition ? func : func)();",
      "\"function (this:F): ?\" must be called with a \"this\" type");
}
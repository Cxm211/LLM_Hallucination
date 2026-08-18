// com/google/javascript/jscomp/TypeCheckTest.java
public void testThisTypeOfFunction6() throws Exception {
    testTypes(
        "/** @constructor */ function F() { this.method = function() {}; }" +
        "var obj = new F();" +
        "var m = obj.method;" +
        "m();",
        "\"function (this:F): undefined\" must be called with a \"this\" type");
  }

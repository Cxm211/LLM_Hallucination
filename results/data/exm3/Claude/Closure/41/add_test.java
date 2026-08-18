// com/google/javascript/jscomp/TypeCheckTest.java
public void testMethodInferenceMultipleParams() throws Exception {
    testTypes(
        "/** @constructor */ function F() {}" +
        "/** @param {number} x @param {string} y */ F.prototype.foo = function(x, y) { };" +
        "/** @constructor \n * @extends {F} */ " +
        "function G() {}" +
        "/** @override */ G.prototype.foo = function(a, b) { };" +
        "(new G()).foo('str', 1);",
        "actual parameter 1 of G.prototype.foo does not match formal parameter\n" +
        "found   : string\n" +
        "required: number");
  }
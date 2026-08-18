// com/google/javascript/jscomp/LooseTypeCheckTest.java
public void testMethodInferenceMultipleParams() throws Exception {
    testTypes(
        "/** @constructor */ function F() {}" +
        "/** @param {number} x @param {string} y */ F.prototype.foo = function(x, y) { };" +
        "/** @constructor \n * @extends {F} */ " +
        "function G() {}" +
        "/** @override */ G.prototype.foo = function(a, b) { };" +
        "(new G()).foo('str', 1);");
  }
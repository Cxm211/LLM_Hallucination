// com/google/javascript/jscomp/LooseTypeCheckTest.java
public void testMethodInferenceAdditional1() throws Exception {
  testTypes(
      "/** @constructor */ function F() {}" +
      "F.prototype.foo = function(x) { };" +
      "/** @constructor \n * @extends {F} */ " +
      "function G() {}" +
      "/** @override */ G.prototype.foo = function() { };",
      "mismatch of the foo property type and the type of the property " +
      "it overrides from superclass F\n" +
      "original: function (this:F, ?): undefined\n" +
      "override: function (this:G): undefined");
}
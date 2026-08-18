// com/google/javascript/jscomp/TypeCheckTest.java
public void testOverrideViaInterfaceExtends() throws Exception {
  testTypes(
      "/** @interface */ function I1() {}" +
      "/** @return {number} */ I1.prototype.get = function(){};" +
      "/** @interface \n * @extends {I1} */ function I2() {}" +
      "/** @constructor \n * @implements {I2} */ function F() {}" +
      "/** @override */ F.prototype.get = function() { return true; };",
      "inconsistent return type\n" +
      "found   : boolean\n" +
      "required: number");
}

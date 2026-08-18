// com/google/javascript/jscomp/TypeCheckTest.java::testOverrideFromExtendedInterface
public void testOverrideFromExtendedInterface() throws Exception {
    testTypes(
        "/** @interface */ function J() {}" +
        "/** @return {number} */ J.prototype.m = function(){};" +
        "/** @interface \n * @extends {J} */ function I() {}" +
        "/** @constructor \n * @implements {I} */ function F() {}" +
        "/** @override */ F.prototype.m = function() { return true; };",
        "inconsistent return type\n" +
        "found   : boolean\n" +
        "required: number");
  }
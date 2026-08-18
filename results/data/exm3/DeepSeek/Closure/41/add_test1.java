// com/google/javascript/jscomp/TypeCheckTest.java
public void testOverrideVarArgs() throws Exception {
    testTypes(
        "/** @constructor */ function C() {}" +
        "/** @param {...number} nums */ C.prototype.sum = function(nums) {};" +
        "/** @constructor @extends {C} */ function D() {}" +
        "/** @override */ D.prototype.sum = function() {};" +
        "(new D()).sum(1, 2, 3);");
  }

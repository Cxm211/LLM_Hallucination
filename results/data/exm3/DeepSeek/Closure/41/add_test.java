// com/google/javascript/jscomp/TypeCheckTest.java
public void testOverrideFewerParameters() throws Exception {
    testTypes(
        "/** @constructor */ function A() {}" +
        "/** @param {number} x @param {string} y */ A.prototype.f = function(x,y) {};" +
        "/** @constructor @extends {A} */ function B() {}" +
        "/** @override */ B.prototype.f = function() {};" +
        "(new B()).f(1, 'hello');");
  }

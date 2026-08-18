// com/google/javascript/jscomp/AmbiguatePropertiesTest.java
public void testMultipleInterfacesWithSameProperty() {
  String js = ""
      + "/** @interface */ function I1() {}\n"
      + "I1.prototype.foo = function() {};\n"
      + "/** @interface */ function I2() {}\n"
      + "I2.prototype.foo = function() {};\n"
      + "/**\n"
      + " * @constructor\n"
      + " * @implements {I1}\n"
      + " * @implements {I2}\n"
      + " */\n"
      + "function C() {}\n"
      + "C.prototype.foo = function() { return 1; };\n"
      + "/** @param {I1} x */ function f1(x) { x.foo(); }\n"
      + "/** @param {I2} x */ function f2(x) { x.foo(); }\n"
      + "/** @param {C} x */ function f3(x) { x.foo(); }";
  String output = ""
      + "function I1() {}\n"
      + "I1.prototype.a = function() {};\n"
      + "function I2() {}\n"
      + "I2.prototype.a = function() {};\n"
      + "function C() {}\n"
      + "C.prototype.a = function() { return 1; };\n"
      + "function f1(x) { x.a(); }\n"
      + "function f2(x) { x.a(); }\n"
      + "function f3(x) { x.a(); }";
  test(js, output);
}
// com/google/javascript/jscomp/AmbiguatePropertiesTest.java
public void testImplementsInterfaceWithInheritedProperty() {
  String js = ""
      + "/** @interface */ function BaseI() {}\n"
      + "BaseI.prototype.prop = function() {};\n"
      + "/** @interface @extends {BaseI} */ function ExtI() {}\n"
      + "/**\n"
      + " * @constructor\n"
      + " * @implements {ExtI}\n"
      + " */\n"
      + "function Impl() {}\n"
      + "Impl.prototype.prop = function() { return 5; };\n"
      + "/** @param {ExtI} x */ function f(x) { x.prop(); }";
  String output = ""
      + "function BaseI() {}\n"
      + "BaseI.prototype.a = function() {};\n"
      + "function ExtI() {}\n"
      + "function Impl() {}\n"
      + "Impl.prototype.a = function() { return 5; };\n"
      + "function f(x) { x.a(); }";
  test(js, output);
}
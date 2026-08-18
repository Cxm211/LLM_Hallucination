// com/google/javascript/jscomp/AmbiguatePropertiesTest.java
public void testInterfaceProperty() {
  String js = "/** @interface */ function I() {}\nI.prototype.foo = function() {};\n/** @param {I} x */ function f(x) { x.foo(); }";
  String output = "function I(){}\nI.prototype.a = function() {};\nfunction f(x) { x.a(); }";
  test(js, output);
}

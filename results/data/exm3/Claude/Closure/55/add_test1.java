// com/google/javascript/jscomp/FunctionRewriterTest.java
public void testMixedGetterSetterNotReduced() {
  checkCompilesToSame(
      "var obj = {\n" +
      "  get prop() { return this._prop; },\n" +
      "  set prop(val) { this._prop = val; }\n" +
      "};", 1);
}
// com/google/javascript/jscomp/FunctionRewriterTest.java
public void testSetterFunctionNotReduced() {
  checkCompilesToSame(
      "var obj = {\n" +
      "  set prop(val) { this._prop = val; }\n" +
      "};", 1);
}
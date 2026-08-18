// com/google/javascript/jscomp/CollapsePropertiesTest.java
public void testNestedFunctionWithArgumentsAlias() {
  collapsePropertiesOnExternTypes = true;
  testSame(
    "function outer() {\n" +
    "  function inner() {\n" +
    "    var a = arguments;\n" +
    "    return function() { return a[0]; };\n" +
    "  }\n" +
    "  return inner;\n" +
    "}\n");
}
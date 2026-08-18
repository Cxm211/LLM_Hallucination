// com/google/javascript/jscomp/CollapsePropertiesTest.java
public void testArgumentsWithProperties() {
  collapsePropertiesOnExternTypes = true;
  testSame(
    "function f() {\n" +
    "  var args = arguments;\n" +
    "  args.length;\n" +
    "  return args;\n" +
    "}\n");
}
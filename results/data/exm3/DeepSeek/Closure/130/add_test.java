// com/google/javascript/jscomp/CollapsePropertiesTest.java
public void testExternAliasNotInlinedWhenFlagFalse() {
  collapsePropertiesOnExternTypes = false;
  testSame(new String[] {"var myExtern;"},
      "myExtern = {};\n" +
      "function f() {\n" +
      "  var alias = myExtern;\n" +
      "  return alias;\n" +
      "}");
}

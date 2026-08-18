// com/google/javascript/jscomp/CollapsePropertiesTest.java
public void testArgumentsAliasWithMultipleRefs() {
  collapsePropertiesOnExternTypes = true;
  testSame(
    "function f() {\n" +
    "  var args = arguments;\n" +
    "  alert(args);\n" +
    "  setTimeout(function() { alert(args); }, 0);\n" +
    "}\n");
}
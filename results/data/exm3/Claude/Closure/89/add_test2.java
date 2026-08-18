// com/google/javascript/jscomp/CollapsePropertiesTest.java
public void testNoCollapseWithMultipleGlobalSets() {
  testSame("var a = {}; a.b = 1; a.b = 2; a.b;");
}
// com/google/javascript/jscomp/CollapsePropertiesTest.java
public void testAliasCreatedForCtorDepth1() {
  testSame("/** @constructor */ var a = function(){}; a.b = 1; var c = a; a.b;");
}
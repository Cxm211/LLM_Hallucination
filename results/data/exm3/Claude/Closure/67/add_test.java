// com/google/javascript/jscomp/RemoveUnusedPrototypePropertiesTest.java
public void testAliasing8() {
  // Test nested property access with prototype in different position
  testSame("function e(){}" +
         "e.prototype.method1 = function(){};" +
         "var alias = e.prototype;");
}
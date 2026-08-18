// com/google/javascript/jscomp/RemoveUnusedPrototypePropertiesTest.java
public void testAliasing9() {
  // Test single level property assignment (non-prototype)
  test("function e(){}" +
       "e.method1 = function(){};",
       "function e(){}");
}
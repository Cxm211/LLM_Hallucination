// com/google/javascript/jscomp/RemoveUnusedPrototypePropertiesTest.java
public void testAliasing10() {
  // Test chained assignment without prototype keyword
  test("function e(){}" +
       "e.foo.bar = function(){};",
       "function e(){}");
}
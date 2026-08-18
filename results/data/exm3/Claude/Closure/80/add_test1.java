// com/google/javascript/jscomp/NodeUtilTest.java
public void testLocalValue2() throws Exception {
  // Test delete operator - always returns local boolean
  assertTrue(testLocalValue("delete x.y"));
  assertTrue(testLocalValue("delete obj['prop']"));
  
  // Test nested delete in expressions
  assertTrue(testLocalValue("x = delete y.z"));
  assertTrue(testLocalValue("(a, delete b.c)"));
}
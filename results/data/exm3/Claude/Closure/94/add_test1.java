// com/google/javascript/jscomp/NodeUtilTest.java
public void testValidDefineWithNestedOperations() {
  assertTrue(testValidDefineValue("(1 + 2) & 8"));
  assertTrue(testValidDefineValue("!(1 + 2)"));
  assertFalse(testValidDefineValue("(x + 2) & 8"));
}
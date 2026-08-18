// com/google/javascript/jscomp/NodeUtilTest.java
public void testValidDefineWithBitwiseOperations() {
  assertTrue(testValidDefineValue("1 | 8"));
  assertTrue(testValidDefineValue("1 ^ 8"));
  assertTrue(testValidDefineValue("~5"));
  assertFalse(testValidDefineValue("x | 8"));
  assertFalse(testValidDefineValue("1 | y"));
}
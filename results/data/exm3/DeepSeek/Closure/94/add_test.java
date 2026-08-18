// com/google/javascript/jscomp/NodeUtilTest.java
public void testValidDefineBitwiseOperators() {
    // Valid bitwise operations with constants
    assertTrue(testValidDefineValue("1 | 2"));
    assertTrue(testValidDefineValue("1 ^ 2"));
    // Invalid because one operand is not a define
    assertFalse(testValidDefineValue("1 | foo"));
    assertFalse(testValidDefineValue("1 ^ foo"));
  }

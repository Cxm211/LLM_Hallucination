// com/google/javascript/jscomp/NodeUtilTest.java
public void testValidDefine_BitOrInvalid() {
    assertFalse(testValidDefineValue("1 | foo"));
  }
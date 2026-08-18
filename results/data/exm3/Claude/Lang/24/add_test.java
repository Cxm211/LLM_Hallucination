// org/apache/commons/lang3/math/NumberUtilsTest.java
public void testIsNumberAdditional1() {
    String val = "123L";
    assertTrue("isNumber(String) should accept integer with L", NumberUtils.isNumber(val));
    val = "0L";
    assertTrue("isNumber(String) should accept 0L", NumberUtils.isNumber(val));
    val = "-456L";
    assertTrue("isNumber(String) should accept negative integer with L", NumberUtils.isNumber(val));
    val = "1.0L";
    assertFalse("isNumber(String) should reject decimal with L", NumberUtils.isNumber(val));
    val = "1.5L";
    assertFalse("isNumber(String) should reject decimal with L", NumberUtils.isNumber(val));
    val = "1E5L";
    assertFalse("isNumber(String) should reject exponent with L", NumberUtils.isNumber(val));
}
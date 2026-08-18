// org/apache/commons/lang3/math/NumberUtilsTest.java
@Test
public void testCreateNumberDoubleWithAllZeros() {
    Number result = NumberUtils.createNumber("0.0D");
    assertTrue("Should return Double for 0.0D", result instanceof Double);
    assertEquals("Value should be 0.0", 0.0, result.doubleValue(), 0.0);
}
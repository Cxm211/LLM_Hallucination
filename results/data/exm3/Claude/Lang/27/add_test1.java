// org/apache/commons/lang3/math/NumberUtilsTest.java
@Test
public void testCreateNumberDoubleNonZero() {
    Number result = NumberUtils.createNumber("1234.5678D");
    assertTrue("Should return Double", result instanceof Double);
    assertEquals("Value should be 1234.5678", 1234.5678, result.doubleValue(), 0.0001);
}
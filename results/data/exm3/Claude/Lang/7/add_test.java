// org/apache/commons/lang3/math/NumberUtilsTest.java
@Test
public void testCreateNumberWithDSuffix() {
    final Number num1 = NumberUtils.createNumber("-1.1E-700D");
    assertEquals("Expected BigDecimal for very small double with D suffix", BigDecimal.class, num1.getClass());
    assertNotNull(num1);
    
    final Number num2 = NumberUtils.createNumber("1.1E-700D");
    assertEquals("Expected BigDecimal for very small positive double with D suffix", BigDecimal.class, num2.getClass());
    assertNotNull(num2);
    
    final Number num3 = NumberUtils.createNumber("-1.1E-700d");
    assertEquals("Expected BigDecimal for very small double with d suffix", BigDecimal.class, num3.getClass());
    assertNotNull(num3);
}
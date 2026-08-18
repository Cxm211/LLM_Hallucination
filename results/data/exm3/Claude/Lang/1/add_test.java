// org/apache/commons/lang3/math/NumberUtilsTest.java
@Test
public void testHexBoundariesWithNegative() {
    assertEquals(Integer.valueOf(-0x7FFFFFFF), NumberUtils.createNumber("-0x7FFFFFFF"));
    assertEquals(Long.valueOf(-0x80000000L), NumberUtils.createNumber("-0x80000000"));
    assertEquals(Long.valueOf(-0x7FFFFFFFFFFFFFFFL), NumberUtils.createNumber("-0x7FFFFFFFFFFFFFFF"));
    assertEquals(new BigInteger("-8000000000000000", 16), NumberUtils.createNumber("-0x8000000000000000"));
}
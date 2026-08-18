// org/apache/commons/lang3/math/NumberUtilsTest.java
@Test
public void testHexWithHashPrefix() {
    assertEquals(Integer.valueOf(0x7FFF), NumberUtils.createNumber("#7FFF"));
    assertEquals(Integer.valueOf(0x7FFFFFFF), NumberUtils.createNumber("#7FFFFFFF"));
    assertEquals(Long.valueOf(0x80000000L), NumberUtils.createNumber("#80000000"));
    assertEquals(Long.valueOf(0x7FFFFFFFFFFFFFFFL), NumberUtils.createNumber("#7FFFFFFFFFFFFFFF"));
    assertEquals(new BigInteger("8000000000000000", 16), NumberUtils.createNumber("#8000000000000000"));
    assertEquals(Integer.valueOf(-0x7FFF), NumberUtils.createNumber("-#7FFF"));
    assertEquals(Long.valueOf(-0x80000000L), NumberUtils.createNumber("-#80000000"));
}
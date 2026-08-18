// org/apache/commons/lang3/math/NumberUtilsTest.java
@Test
public void testHexExactly9And17Digits() {
    assertEquals(Long.valueOf(0x100000000L), NumberUtils.createNumber("0x100000000"));
    assertEquals(new BigInteger("10000000000000000", 16), NumberUtils.createNumber("0x10000000000000000"));
    assertEquals(Long.valueOf(0x1FFFFFFFFL), NumberUtils.createNumber("0x1FFFFFFFF"));
    assertEquals(new BigInteger("1FFFFFFFFFFFFFFFF", 16), NumberUtils.createNumber("0x1FFFFFFFFFFFFFFFF"));
}
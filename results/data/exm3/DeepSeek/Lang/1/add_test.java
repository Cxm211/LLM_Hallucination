// org/apache/commons/lang3/math/NumberUtilsTest.java
@Test
    public void testHexNegativeAndHash() {
        // Negative hex numbers
        assertEquals(Integer.valueOf(-0x1), NumberUtils.createNumber("-0x1"));
        assertEquals(Long.valueOf(-0x80000000L), NumberUtils.createNumber("-0x80000000"));
        assertEquals(new BigInteger("-8000000000000000", 16), NumberUtils.createNumber("-0x8000000000000000"));
        // Hash prefix
        assertEquals(Integer.valueOf(0xFF), NumberUtils.createNumber("#FF"));
        assertEquals(Long.valueOf(0xFFFFFFFFL), NumberUtils.createNumber("#FFFFFFFF"));
        assertEquals(Long.valueOf(0x100000000L), NumberUtils.createNumber("#100000000"));
    }

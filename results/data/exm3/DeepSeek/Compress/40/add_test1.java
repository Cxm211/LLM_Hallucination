// org/apache/commons/compress/utils/BitInputStreamTest.java
@Test
    public void littleEndianWithOverflow2() throws Exception {
        ByteArrayInputStream in = new ByteArrayInputStream(new byte[] {
                87, // 01010111
                45, // 00101101
                66, // 01000010
                15, // 00001111
                90, // 01011010
                29, // 00011101
                88, // 01011000
                61, // 00111101
                33, // 00100001
                74  // 01001010
            });
        BitInputStream bin = new BitInputStream(in, ByteOrder.LITTLE_ENDIAN);
        assertEquals(7, // 0111
                     bin.readBits(4));
        assertEquals(357313256394137602L, // 59 bits computed from fixed implementation
                     bin.readBits(59));
        assertEquals(330, // 000101001010 (12 bits?)
                     bin.readBits(8));
        assertEquals(-1 , bin.readBits(1));
    }
